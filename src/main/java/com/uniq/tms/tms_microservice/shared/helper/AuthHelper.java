package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.shared.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    public CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        } else {
            throw new RuntimeException("Unauthenticated or Invalid user");
        }
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
