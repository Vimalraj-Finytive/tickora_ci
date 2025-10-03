package com.uniq.tms.tms_microservice.modules.locationManagement.services;

import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LocationCacheService {
    CompletableFuture<List<Location>> loadLocationTable(String orgId, String schema);
}
