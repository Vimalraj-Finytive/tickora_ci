package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.shared.security.jwt.JwtUtil;
import com.uniq.tms.tms_microservice.shared.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {
    private final JwtUtil jwtUtil;

    public AuthHelper(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user found.");
    }

    public String getUserId() {
        return getCurrentUser().getUserId();
    }

    public String getOrgId() {
        return getCurrentUser().getOrgId();
    }

    public String getUsername() {
        return getCurrentUser().getUsername();
    }

    public String getRole() {
        return getCurrentUser().getRole();
    }

    public String getSchema(){ return getCurrentUser().getSchemaName(); }
}
