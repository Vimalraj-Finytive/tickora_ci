package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;

public interface OrganizationCacheService {
    void loadAllRolesToCache(String orgId, String schema);
    void loadPrivilegesFromDB(String schema);
    String getPrivilegeKey(PrivilegeConstants constant);
    boolean isOrderIdUsed(String schema, String orgId, String orderId);
    void markOrderIdUsed(String schema, String orgId, String orderId);

}
