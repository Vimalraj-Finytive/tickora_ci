package com.uniq.tms.tms_microservice.config;

import com.uniq.tms.tms_microservice.adapter.AuthAdapter;
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
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.logging.log4j.Logger;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyBase64;

    private static final Logger log  = LogManager.getLogger(JwtUtil.class);


    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthAdapter authAdapter;

    private static final long INACTIVITY_TIMEOUT = 90L * 24 * 60 * 60 * 1000;

    public JwtUtil(BlacklistedTokenRepository blacklistedTokenRepository, UserRepository userRepository, RoleRepository roleRepository, OrganizationRepository organizationRepository, AuthAdapter authAdapter) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.authAdapter = authAdapter;
    }
    public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(String loginInput, long l,  HttpServletRequest request) {
        log.info("generating token for: " + loginInput);
        // List of Suppliers returning UserEntity
        log.info("checking User by logged input: {}");
        List<Supplier<UserEntity>> users = List.of(
                () -> loginInput.contains("@") ? userRepository.findByEmail(loginInput) : null,
                () -> loginInput.matches("\\d{10}") ? authAdapter.findByMobileNumber(loginInput) : null,
                () -> authAdapter.findStudentIdByMobile(loginInput)
        );

        // Use Stream to get the first non-null user
        UserEntity user = users.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found for input: " + loginInput));

        log.info("user found: {}", user.getEmail());
        Long roleId = user.getRole().getRoleId();
        log.info("Fetching role id for logged user: {}", roleId);
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        log.info("role: {} not found for the given roleId", role);
        String orgId = user.getOrganizationId();

        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        long currentTime = System.currentTimeMillis();
        String userAgent = request.getHeader("User-Agent");

        log.info("token generated for: " + loginInput);

        return Jwts.builder()
                .setSubject(loginInput)
                .claim("roles", "ROLE_" + role.getName())
                .claim("orgId", user.getOrganizationId())
                .claim("userId", user.getUserId())
                .claim("userAgent", userAgent)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + INACTIVITY_TIMEOUT))
                .claim("lastActiveTime", currentTime)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim(); // First IP = real client
        }
        return request.getRemoteAddr(); // Fallback
    }

    public Claims parseToken(String token, String requestUserAgent) {
        log.info("parsing token: {}", token);

        if (isTokenBlacklisted(token)) {
            throw new SecurityException("Token is blacklisted");
        }
        try {
            Claims claims = extractAllClaims(token);
            long lastActiveTime = claims.get("lastActiveTime", Long.class);
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastActiveTime > INACTIVITY_TIMEOUT) {
                log.warn("token has expired due to inactivity: {}", token);
                throw new SecurityException("Token has expired due to inactivity");
            }
            log.info("claims useragent {}", requestUserAgent);
            String tokenUserAgent = claims.get("userAgent", String.class);

            if (!requestUserAgent.equals(tokenUserAgent)) {
                log.info("user agent mismatch: {}", token);
                throw new SecurityException("Token mismatch: Possible token theft or misuse detected");
            }
            claims.put("lastActiveTime", currentTime);
            return claims;
        } catch (ExpiredJwtException e) {
            log.info("token has expired: {} ", token, e);
            throw new SecurityException("Token has expired", e);
        }
        catch (Exception e) {
            log.info("invalid token: {}", token, e);
            throw new SecurityException("Invalid Token", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        log.info("checking if token is blacklisted: {}", token);
        boolean isBlacklisted = blacklistedTokenRepository.existsByToken(token);
        log.info("token backlist status is: {}", isBlacklisted ? "blacklisted" : "not blacklisted");
        return isBlacklisted;
    }

    public void blacklistToken(String token,  String requestUserAgent) {
        log.info("blacklisting token: {}", token);
        try {
            System.out.println("JWT_Util _________userAgent from the token: {}"+ requestUserAgent);
            System.out.println("JWT_Util ________AuthHeader from the token: {}"+ token);

            log.info("parsing token: {}", token);
            Claims claims = parseToken(token, requestUserAgent);
            String userEmail = claims.getSubject();
            log.info("find user by token");
            UserEntity userEntity = userRepository.findByEmail(userEmail);
            BlacklistedTokenEntity blacklistedTokenEntity = new BlacklistedTokenEntity();
            blacklistedTokenEntity.setToken(token);
            blacklistedTokenEntity.setUser(userEntity);
            blacklistedTokenEntity.setLoggedOutAt(LocalDateTime.now());
            blacklistedTokenRepository.save(blacklistedTokenEntity);
            log.info("token blacklisted: {}", token);
        } catch (Exception e) {
            log.info("error blacklisting token: {}", e);
            throw new RuntimeException("Error blacklisting token", e);
        }
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

    public String extractOrgIdFromToken(String token) {
        log.info("extracting orgId from token: {}", token);
        Claims claims = extractAllClaims(token);

        if (claims.containsKey("orgId")) {
            Object orgId = claims.get("orgId");
            log.info("orgId: {} found in JWT claims", orgId);
            return (orgId instanceof String) ? ((String) orgId) : null;
        }
        log.info("orgId not found in JWT claims");
        throw new IllegalStateException("Organization ID not found in JWT claims!");
    }

    public String extractUsername(String token) {
        log.info("extracting username from token: {}", token);
        return extractClaim(token, Claims::getSubject);
    }
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractJwt(String authHeader) {
        log.info("extracting jwt from header: {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("invalid authorization header: {}", authHeader);
            throw new RuntimeException("Invalid Authorization header");
        }
        return authHeader.substring(7);
    }

    public String extractRoleFromToken(String jwt) {
        log.info("extracting role from token: {}", jwt);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            log.info("role found in JWT claims");
            return claims.get("roles", String.class);
        } catch (SignatureException e) {
            log.info("Invalid JWT signature: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public Long extractUserIdFromToken(String token) {
        log.info("extracting userId from token: {}", token);
        Claims claims = extractAllClaims(token);
        log.info("userId: {} found in JWT claims", claims.get("userId"));
        return claims.get("userId", Long.class);
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
