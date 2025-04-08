package com.uniq.tms.tms_microservice.config;


import com.uniq.tms.tms_microservice.entity.BlacklistedTokenEntity;
import com.uniq.tms.tms_microservice.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.repository.BlacklistedTokenRepository;
import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;


@Component
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Autowired
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    private static final long INACTIVITY_TIMEOUT = 12 * 60 * 60 * 1000;

    public JwtUtil(BlacklistedTokenRepository blacklistedTokenRepository, UserRepository userRepository, RoleRepository roleRepository, OrganizationRepository organizationRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
    }

    public String generateToken(String email, long l,  HttpServletRequest request) {
        UserEntity user = userRepository.findByEmail(email);

        Long roleId = user.getRole().getRoleId();

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Long orgId = user.getOrganizationId();

        OrganizationEntity organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        long currentTime = System.currentTimeMillis();
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", "ROLE_" + role.getName())
                .claim("orgId", user.getOrganizationId())
                .claim("userAgent", userAgent)
                .claim("ipAddress", ipAddress)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + INACTIVITY_TIMEOUT))
                .claim("lastActiveTime", currentTime)
                .signWith(SECRET_KEY)
                .compact();
    }

    public Claims parseToken(String token,  String requestUserAgent, String requestIp) {
        if (isTokenBlacklisted(token)) {
            throw new SecurityException("Token is blacklisted");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            long lastActiveTime = claims.get("lastActiveTime", Long.class);
            if (System.currentTimeMillis() - lastActiveTime > INACTIVITY_TIMEOUT) {
                throw new SecurityException("Token has expired due to inactivity");
            }

            return claims;
        } catch (ExpiredJwtException e) {
            throw new SecurityException("Token has expired", e);
        }
        catch (Exception e) {
            throw new SecurityException("Invalid Token", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    public void blacklistToken(String token,  String requestUserAgent, String requestIp) {
        try {
            Claims claims = parseToken(token, requestUserAgent, requestIp);
            String userEmail = claims.getSubject();

            UserEntity userEntity = userRepository.findByEmail(userEmail);

            BlacklistedTokenEntity blacklistedTokenEntity = new BlacklistedTokenEntity();
            blacklistedTokenEntity.setToken(token);
            blacklistedTokenEntity.setUser(userEntity);
            blacklistedTokenEntity.setLoggedOutAt(LocalDateTime.now());

            blacklistedTokenRepository.save(blacklistedTokenEntity);
        } catch (Exception e) {
            throw new RuntimeException("Error blacklisting token", e);
        }
    }

    public String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Claims extractAllClaims(String token) {
        System.out.println("JWT Token Received: " + token);
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractOrgIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        System.out.println("JWT Claims: " + claims);

        if (claims.containsKey("orgId")) {
            Object orgId = claims.get("orgId");
            System.out.println("Extracted orgId from JWT Claims: " + orgId);
            return (orgId instanceof Integer) ? ((Integer) orgId).longValue() : Long.parseLong(orgId.toString());
        }

        System.out.println("Organization ID not found in JWT claims!");
        return null;
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractJwt(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header");
        }
        return authHeader.substring(7);
    }


    public String extractRoleFromToken(String jwt) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            System.out.println(claims.get("roles", String.class));
            return claims.get("roles", String.class);
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }


}