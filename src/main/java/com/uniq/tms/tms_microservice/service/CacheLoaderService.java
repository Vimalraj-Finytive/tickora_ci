package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.model.Location;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CacheLoaderService {
    CompletableFuture<Map<String, List<UserResponseDto>>> loadAllUsers(String orgId);
    CompletableFuture<List<Location>> loadLocationTable(String orgId);
    void loadAllRolesToCache();
    void loadPrivilegesFromDB();
    String getPrivilegeKey(PrivilegeConstants constant);
    CompletableFuture<Map<String, UserProfileResponse>> loadUsersProfile(String orgId);
    CompletableFuture<List<GroupResponseDto>> loadGroupsCache(String orgId);
    CompletableFuture<Map<String, List<WorkScheduleDto>>> loadWorkSchedule(String orgId);
    CompletableFuture<Map<String, List<UserResponseDto>>> loadAllInactiveUsers(String orgId);
}
