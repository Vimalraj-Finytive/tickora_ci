package com.uniq.tms.tms_microservice.config.security.jwt;

import com.uniq.tms.tms_microservice.adapter.AuthAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.PrivilegeConstants;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.helper.RolePrivilegeHelper;
import com.uniq.tms.tms_microservice.repository.*;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.*;
import org.apache.logging.log4j.Logger;

@Component
public class JwtUtil {

    @Value("${app.api.key}")
    private String secretKeyBase64;

    private static final Logger log  = LogManager.getLogger(JwtUtil.class);

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthAdapter authAdapter;
    private final CacheLoaderService cacheLoaderService;
    private final SecondaryDetailsRepository secondaryDetailsRepository;
    private final UserAdapter userAdapter;
    private final RolePrivilegeHelper rolePrivilegeHelper;

    private static final long INACTIVITY_TIMEOUT = 90L * 24 * 60 * 60 * 1000;

    public JwtUtil(BlacklistedTokenRepository blacklistedTokenRepository, UserRepository userRepository, RoleRepository roleRepository, OrganizationRepository organizationRepository, AuthAdapter authAdapter, CacheLoaderService cacheLoaderService, SecondaryDetailsRepository secondaryDetailsRepository, UserAdapter userAdapter, RolePrivilegeHelper rolePrivilegeHelper) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.authAdapter = authAdapter;
        this.cacheLoaderService = cacheLoaderService;
        this.secondaryDetailsRepository = secondaryDetailsRepository;
        this.userAdapter = userAdapter;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
    }

    @PersistenceContext
    private EntityManager entityManager;

    public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String loginInput, long l, HttpServletRequest request, boolean hasEmailLoginPrivilege, String currentTenant) {
        log.info("generating token for: {}", loginInput);
        UserEntity loggedInUser = null;
        UserEntity studentUser = null;
        String parentUserId = null;

        log.info("has email login privilege: {}", hasEmailLoginPrivilege);
        if (hasEmailLoginPrivilege) {
            loggedInUser = userRepository.findByEmail(loginInput);

        } else {
            loggedInUser = authAdapter.findByMobileNumber(loginInput);
            if (loggedInUser == null) {
                loggedInUser = authAdapter.findStudentIdByMobile(loginInput);
                if (loggedInUser != null) {
                    Optional<SecondaryDetailsEntity> parentEntity = authAdapter.findParentByMobile(loginInput);
                    parentUserId = parentEntity.map(SecondaryDetailsEntity::getId).orElse(null);
                }
            }
        }

        if (loggedInUser == null) {
            throw new UsernameNotFoundException("User not found for input: " + loginInput);
        }

        RoleEntity role = null;
        if (loggedInUser.getRole() != null) {
            role = roleRepository.findById(loggedInUser.getRole().getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
        }

        String orgId = loggedInUser.getOrganizationId();

        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        long currentTime = System.currentTimeMillis();
        String userAgent = request.getHeader("User-Agent");

        JwtBuilder builder = Jwts.builder()
                .setSubject(loginInput)
                .claim("orgId", orgId)
                .claim("userId", loggedInUser.getUserId())
                .claim("userSchema",currentTenant)
                .claim("userAgent", userAgent)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + INACTIVITY_TIMEOUT))
                .claim("lastActiveTime", currentTime);

        if (role != null) {
            builder.claim("roles", "ROLE_" + role.getName());
        }

        if (parentUserId != null) {
            builder.claim("parentUserId", parentUserId);
        }

        return builder.signWith(getSecretKey(), SignatureAlgorithm.HS256).compact();
    }

    public void blacklistToken(String token) {
        log.info("Blacklisting token: {}", token);
        try {
            Claims claims = extractAllClaims(token);
            String subject = claims.getSubject();
            UserEntity user = findUserFromClaims(subject, claims);
            BlacklistedTokenEntity entity = new BlacklistedTokenEntity();
            entity.setToken(token);
            entity.setUser(user);
            entity.setLoggedOutAt(LocalDateTime.now());
            blacklistedTokenRepository.save(entity);

        } catch (Exception e) {
            log.error("Failed to blacklist token (might be already expired): {}", e.getMessage());
            BlacklistedTokenEntity entity = new BlacklistedTokenEntity();
            entity.setToken(token);
            entity.setLoggedOutAt(LocalDateTime.now());
            blacklistedTokenRepository.save(entity);
        }
    }

    private UserEntity findUserFromClaims(String subject, Claims claims) {
        String role = claims.get("roles", String.class);
        if (role != null) {
            role = role.replace("ROLE_", "");
        }
        String emailKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_EMAIL);
        String mobileKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_MOBILE);
        UserEntity user = null;
        if (rolePrivilegeHelper.roleHasPrivilege(role, emailKey)) {
            user = userRepository.findByEmail(subject);
        }
        else if (rolePrivilegeHelper.roleHasPrivilege(role, mobileKey)) {
            user = userRepository.findByMobileNumber(subject);
            if (user == null) {
                user = secondaryDetailsRepository.findUserByMobile(subject);
            }
        }
        return user;
    }

    public Claims parseAndValidateToken(String token, String requestUserAgent) {
        if (isTokenBlacklisted(token)) {
            throw new SecurityException("Token is blacklisted");
        }

        Claims claims = extractAllClaims(token);

        long lastActiveTime = claims.get("lastActiveTime", Long.class);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastActiveTime > INACTIVITY_TIMEOUT) {
            throw new SecurityException("Token expired due to inactivity");
        }

        String tokenUserAgent = claims.get("userAgent", String.class);
        if (!requestUserAgent.equals(tokenUserAgent)) {
            throw new SecurityException("User agent mismatch - possible token theft");
        }
        return claims;
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }


    public String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            log.info("extracting jwt from cookies: {}", (Object) request.getCookies());
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    log.info("jwt token found in cookie: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Claims extractAllClaims(String token) {
        log.info("extracting all claims from token: {}", token);
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        log.info("validating token: {}", token);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}
