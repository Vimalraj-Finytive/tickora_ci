package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.model.Location;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CacheLoaderService {
    CompletableFuture<Map<String, List<UserResponseDto>>> loadAllUsers(String orgId, String schema);
    CompletableFuture<List<Location>> loadLocationTable(String orgId, String schema);
    void loadAllRolesToCache(String orgId, String schema);
    void loadPrivilegesFromDB(String schema);
    String getPrivilegeKey(PrivilegeConstants constant);
    CompletableFuture<Map<String, UserProfileResponseDto>> loadUsersProfile(String orgId, String schema);
    CompletableFuture<List<GroupResponseDto>> loadGroupsCache(String orgId, String schema);
    CompletableFuture<Map<String, List<WorkScheduleDto>>> loadWorkSchedule(String orgId, String schema);
    CompletableFuture<Map<String, List<UserResponseDto>>> loadAllInactiveUsers(String orgId, String schema);
}
