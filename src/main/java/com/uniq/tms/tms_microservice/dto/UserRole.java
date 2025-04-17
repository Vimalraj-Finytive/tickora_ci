package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public enum UserRole {
    SUPERADMIN(List.of("Admin", "Manager", "Staff", "Student")),
    ADMIN(List.of("Manager", "Staff", "Student")),
    MANAGER(List.of("Staff", "Student"));

    private final List<String> accessibleRoles;

    UserRole(List<String> accessibleRoles) {
        this.accessibleRoles = accessibleRoles;
    }

    public List<String> getAccessibleRoles() {
        return accessibleRoles;
    }

    public static List<String> getRolesFor(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase()).getAccessibleRoles();
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }
}
