package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.model.Location;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CacheLoaderService {
    CompletableFuture<Map<String, List<UserResponseDto>>> loadAllUsers(Long orgId);
    CompletableFuture<List<Location>> loadLocationTable(Long orgId);
    void loadAllRolesToCache(Long orgId);
    void loadPrivilegesFromDB(Long orgId);
    String getPrivilegeKey(PrivilegeConstants constant);
    CompletableFuture<Map<String, UserProfileResponse>> loadUsersProfile(Long orgId);
    CompletableFuture<List<GroupResponseDto>> loadGroupsCache(Long orgId);
    CompletableFuture<Map<Long, List<WorkScheduleDto>>> loadWorkSchedule(Long orgId);
}
