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
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Component
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Autowired
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    private static final long INACTIVITY_TIMEOUT = 90L * 24 * 60 * 60 * 1000;
    private final AuthAdapter authAdapter;

    public JwtUtil(BlacklistedTokenRepository blacklistedTokenRepository, UserRepository userRepository, RoleRepository roleRepository, OrganizationRepository organizationRepository, AuthAdapter authAdapter) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.authAdapter = authAdapter;
    }

    public String generateToken(String loginInput, long l,  HttpServletRequest request) {
        // List of Suppliers returning UserEntity
        List<Supplier<UserEntity>> userSuppliers = List.of(
                () -> loginInput.contains("@") ? userRepository.findByEmail(loginInput) : null,
                () -> loginInput.matches("\\d{10}") ? authAdapter.findByMobileNumber(loginInput) : null,
                () -> authAdapter.findStudentIdByMobile(loginInput)
        );

        // Use Stream to get the first non-null user
        UserEntity user = userSuppliers.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found for input: " + loginInput));

        Long roleId = user.getRole().getRoleId();

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Long orgId = user.getOrganizationId();

        OrganizationEntity organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        long currentTime = System.currentTimeMillis();
        String userAgent = request.getHeader("User-Agent");
//        String ipAddress = request.getRemoteAddr();
        String ipAddress = getClientIp(request);

        return Jwts.builder()
                .setSubject(loginInput)
                .claim("roles", "ROLE_" + role.getName())
                .claim("orgId", user.getOrganizationId())
                .claim("userId", user.getUserId())
                .claim("userAgent", userAgent)
                .claim("ipAddress", ipAddress)
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(currentTime + INACTIVITY_TIMEOUT))
                .claim("lastActiveTime", currentTime)
                .signWith(SECRET_KEY)
                .compact();
    }


    public String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim(); // First IP = real client
        }
        return request.getRemoteAddr(); // Fallback
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

            String tokenUserAgent = claims.get("userAgent", String.class);
            String tokenIpAddress = claims.get("ipAddress", String.class);

//            if (!requestUserAgent.equals(tokenUserAgent) || !requestIp.equals(tokenIpAddress)) {
//                throw new SecurityException("Token mismatch: Possible token theft or misuse detected");
//            }

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

            System.out.println("JWT_Util _________userAgent from the token: {}"+ requestUserAgent);
            System.out.println("JWT_Util ___________IP Address from the token: {}"+ requestIp);
            System.out.println("JWT_Util ________AuthHeader from the token: {}"+ token);
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
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractOrgIdFromToken(String token) {
        Claims claims = extractAllClaims(token);

        if (claims.containsKey("orgId")) {
            Object orgId = claims.get("orgId");
            return (orgId instanceof Integer) ? ((Integer) orgId).longValue() : Long.parseLong(orgId.toString());
        }
        throw new IllegalStateException("Organization ID not found in JWT claims!");
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
            return claims.get("roles", String.class);
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public Long extractUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }
}
