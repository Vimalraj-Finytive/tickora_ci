package com.uniq.tms.tms_microservice.shared.security.schema;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static void setCurrentUser(String userId){ CURRENT_USER.set(userId);}

    public static String getCurrentUser(){return CURRENT_USER.get();}

    public static void clearUser(){CURRENT_USER.remove();}
}