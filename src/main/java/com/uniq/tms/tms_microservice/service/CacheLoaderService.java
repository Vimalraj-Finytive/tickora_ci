package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.PrivilegeConstants;
import com.uniq.tms.tms_microservice.model.Location;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CacheLoaderService {
    void loadUserTable();
    CompletableFuture<List<Location>> loadLocationTable();
    void loadAllRolesToCache();
    void loadPrivilegesFromDB();
    String getPrivilegeKey(PrivilegeConstants constant);
}
