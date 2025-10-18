package com.uniq.tms.tms_microservice.shared.security.jwt;

import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationCacheService;
import com.uniq.tms.tms_microservice.shared.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.shared.helper.SubscriptionValHelper;
import com.uniq.tms.tms_microservice.shared.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.shared.security.user.CustomUserDetails;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.shared.helper.RolePrivilegeHelper;
import com.uniq.tms.tms_microservice.modules.authenticationManagement.repository.BlacklistedTokenRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.SecondaryDetailsRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@Order(2)
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(JwtFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final SecondaryDetailsRepository secondaryDetailsRepository;
    private final OrganizationCacheService organizationCacheService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RolePrivilegeHelper rolePrivilegeHelper;
    private final SubscriptionValHelper subscriptionValHelper;

    @Autowired
    public JwtFilter(JwtUtil jwtUtil,
                     UserRepository userRepository,
                     SecondaryDetailsRepository secondaryDetailsRepository,
                     OrganizationCacheService organizationCacheService,
                     BlacklistedTokenRepository blacklistedTokenRepository, RolePrivilegeHelper rolePrivilegeHelper, SubscriptionValHelper subscriptionValHelper) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.secondaryDetailsRepository = secondaryDetailsRepository;
        this.organizationCacheService = organizationCacheService;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
        this.subscriptionValHelper = subscriptionValHelper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith("/tms/")) {
            chain.doFilter(request, response);
            return;
        }

        if (isWhiteListed(path)) {
            chain.doFilter(request, response);
            return;
        }

        String jwtToken = extractTokenFromRequest(request);
        String tenant = extractSchemaFromToken(jwtToken);

        if (jwtToken != null) {
            log.info("Tenant:{}", tenant);
            TenantContext.setCurrentTenant(tenant);
            log.info("Current tenant:{}", TenantContext.getCurrentTenant());
            if (blacklistedTokenRepository.existsByToken(jwtToken)) {
                log.warn("JWT token is blacklisted: {}", jwtToken);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is invalid or expired");
                return;
            }

            try {
                Claims claims = jwtUtil.parseAndValidateToken(jwtToken, request.getHeader("User-Agent"));
                String subject = claims.getSubject();
                String role = claims.get("roles", String.class).replace("ROLE_", "");

                UserEntity user = null;
                boolean isParentLogin = false;
                String emailKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_EMAIL);
                log.info("email key : {}", emailKey);
                String mobileKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_MOBILE);
                log.info("mobile key: {}",mobileKey);
                if (rolePrivilegeHelper.roleHasPrivilege(role, emailKey)) {
                    user = userRepository.findByEmail(subject);
                    log.info("user from repo :{}", user);
                    if (!user.isActive()){
                        log.info("Your account is deactivated");
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Your account has been Inactivated.");
                        return;
                    }
                } else if (rolePrivilegeHelper.roleHasPrivilege(role, mobileKey)) {
                    user = userRepository.findByMobileNumber(subject);
                    log.info("mobile user from repo:{}", user);
                    if (user != null && !user.isActive()){
                        log.info("Your account is deactivated.");
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Your account has been Inactivated.");
                        return;
                    }
                }

                if (user == null) {
                    log.info("Student not found. Checking if this is a parent login...");
                    user = secondaryDetailsRepository.findUserByMobile(subject);
                    if (user != null) {
                        isParentLogin = true;
                        log.info("Parent login detected. Student user fetched: {}", user);
                    }
                }

                if (user == null) {
                    log.info("user is null:{}", user);
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User does not exist");
                    return;
                }


                UsernamePasswordAuthenticationToken authentication = buildAuth(user,tenant);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                return;
            }

            log.info("Incoming request URI: {}", path);
            if (!isSubscriptionAllowedPath(path)) {
                log.info("No Path is matched");
                if (!subscriptionValHelper.hasActiveSubscription()) {
                    String message = subscriptionValHelper.getExpiredMessage();
                    log.warn("No active subscription found for org. URI: {}", path);
                    sendErrorResponse(response, HttpServletResponse.SC_PAYMENT_REQUIRED, message);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private String extractSchemaFromToken(String jwtToken) {
        try {
            Claims claims = jwtUtil.extractAllClaims(jwtToken);
            String schema = claims.get("userSchema", String.class);
            if (schema == null || schema.isBlank()) {
                log.warn("Schema not found in token, defaulting to 'public'");
                return "public";
            }
            return schema;
        } catch (Exception e) {
            log.error("Failed to extract schema from token, defaulting to 'public'", e);
            return "public";
        }
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private UsernamePasswordAuthenticationToken buildAuth(UserEntity user, String schema) {
        String userRole = user.getRole().getName().replaceFirst("ROLE_", "");
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getUserId(),
                user.getOrganizationId(),
                user.getRole().getName(),
                user.getUserName(),
                user.getPassword(),
                schema,
                Collections.singletonList(new SimpleGrantedAuthority(userRole))
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        if (response.isCommitted()) {
            log.warn("Response already committed. Skipping error response.");
            return;
        }
        response.resetBuffer();
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String json = String.format("{\"status\":%d,\"message\":\"%s\",\"data\":null}",
                status, message.replace("\"", "\\\""));
        response.getWriter().write(json);
        response.flushBuffer();
    }
    
    private static final List<String> WHITELISTED_PATHS = List.of(
            "/login", "/tms/loginByEmail", "/tms/loginByMobile",
            "/tms/reset-password", "/tms/validate-email",
            "/tms/organization/orgType", "/tms/organization/validate",
            "/tms/organization/create", "/tms/sendOTP",
            "/tms/debug/otpsCount", "/tms/debug/otps",
            "/tms/organization/getDropDowns", "/tms/leaveManagement/countries"
    );

    private static final List<String> SUBSCRIPTION_ALLOWED_PATHS = List.of(
            "/tms/organization/**",
            "/admin/profile"
    );

    private boolean isSubscriptionAllowedPath(String path) {
        return SUBSCRIPTION_ALLOWED_PATHS.stream()
                .anyMatch(allowed -> pathMatcher.match(allowed, path));
    }

    private boolean isWhiteListed(String path){
        return WHITELISTED_PATHS.stream()
                .anyMatch(whitelist -> pathMatcher.match(whitelist,path));
    }

}
