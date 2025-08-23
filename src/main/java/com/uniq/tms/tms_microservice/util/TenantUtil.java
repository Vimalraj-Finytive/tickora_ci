package com.uniq.tms.tms_microservice.util;

import com.uniq.tms.tms_microservice.config.security.schema.TenantContext;

public class TenantUtil {

    public static String getCurrentTenant(){
        return TenantContext.getCurrentTenant();
    }

    public static void setCurrentTenant(String tenant){
        TenantContext.setCurrentTenant(tenant);
    }

    public static void clearTenant(){
        TenantContext.clear();
    }

}
