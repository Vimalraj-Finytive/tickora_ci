package com.uniq.tms.tms_microservice.modules.userManagement.services;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.GroupResponseDto;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserProfileResponseDto;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserResponseDto;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface UserCacheService {
    CompletableFuture<Map<String, List<UserResponseDto>>> loadAllUsers(String orgId, String schema);
    CompletableFuture<Map<String, List<UserResponseDto>>> loadAllInactiveUsers(String orgId, String schema);
    CompletableFuture<Map<String, UserProfileResponseDto>> loadUsersProfile(String orgId, String schema);
    CompletableFuture<List<GroupResponseDto>> loadGroupsCache(String orgId, String schema);

}
