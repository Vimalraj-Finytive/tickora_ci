package com.uniq.tms.tms_microservice.modules.userManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.mapper.LocationDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationTypeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationTypeRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.*;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.shared.util.TextUtil;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.modules.userManagement.model.UserResponse;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.GroupsData;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.SecondaryDetailsRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.GroupRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserLocationRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserCacheService;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class UserCacheServiceImpl implements UserCacheService {

    private final Map<PrivilegeConstants, String> privilegeMap = new ConcurrentHashMap<>();

    private static final Logger log = LogManager.getLogger(UserCacheServiceImpl.class);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserDtoMapper userDtoMapper;
    private final GroupRepository groupRepository;
    private final TextUtil textUtil;
    private final OrganizationTypeRepository organizationTypeRepository;
    private final SecondaryDetailsRepository secondaryDetailsRepository;
    private final CacheKeyUtil cacheKeyUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocationDtoMapper locationDtoMapper;

    public UserCacheServiceImpl(UserRepository userRepository, OrganizationRepository organizationRepository,
                                UserLocationRepository userLocationRepository, UserDtoMapper userDtoMapper,
                                GroupRepository groupRepository, TextUtil textUtil,
                                OrganizationTypeRepository organizationTypeRepository,
                                SecondaryDetailsRepository secondaryDetailsRepository,
                                CacheKeyUtil cacheKeyUtil, @Nullable RedisTemplate<String, Object> redisTemplate, LocationDtoMapper locationDtoMapper) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.userLocationRepository = userLocationRepository;
        this.userDtoMapper = userDtoMapper;
        this.groupRepository = groupRepository;
        this.textUtil = textUtil;
        this.organizationTypeRepository = organizationTypeRepository;
        this.secondaryDetailsRepository = secondaryDetailsRepository;
        this.cacheKeyUtil = cacheKeyUtil;
        this.redisTemplate = redisTemplate;
        this.locationDtoMapper = locationDtoMapper;
    }


    /**
     *
     * @param orgId
     * @return All user list based on logged user hierarchy Level and organization and caches in redis cache key
     */
    public CompletableFuture<Map<String, List<UserResponseDto>>> loadAllUsers(String orgId, String schema) {
        try {
            log.info("Loading all users into cache | tenant: {}", schema);
            String redisKey = cacheKeyUtil.getMemberKey(orgId, schema);
            Map<String, List<UserResponseDto>> roleWiseUserMap = new HashMap<>();

            // Iterate over all roles
            for (UserRole role : UserRole.values()) {
                int hierarchyLevel = UserRole.getLevel(role.name());

                // Fetch from DB
                List<UserResponse> users = userRepository.findAllUsers(orgId, hierarchyLevel);

                if (users.isEmpty()) {
                    log.warn("No users found for orgId={} and role={}", orgId, role);
                    continue;
                }

                // Convert to DTO
                List<UserResponseDto> usersDto = users.stream()
                        .map(userDtoMapper::toDto)
                        .toList();

                // Merge duplicates based on userId
                Map<String, UserResponseDto> mergedUserMap = new LinkedHashMap<>();
                for (UserResponseDto user : usersDto) {
                    mergedUserMap.compute(user.getUserId(), (id, existing) -> {
                        if (existing == null) return user;

                        // Merge group names
                        if (!existing.getGroupName().contains(user.getGroupName().get(0))) {
                            existing.getGroupName().add(user.getGroupName().get(0));
                        }
                        // Merge location names
                        if (!existing.getLocationName().contains(user.getLocationName().get(0))) {
                            existing.getLocationName().add(user.getLocationName().get(0));
                        }
                        return existing;
                    });
                }

                // Save merged users for current role
                roleWiseUserMap.put(role.name(), new ArrayList<>(mergedUserMap.values()));
            }

            if (redisTemplate != null) {
                try {
                    redisTemplate.delete(redisKey);
                    Map<String, Object> redisHashData = new HashMap<>();
                    for (Map.Entry<String, List<UserResponseDto>> entry : roleWiseUserMap.entrySet()) {
                        redisHashData.put(entry.getKey().toLowerCase(), entry.getValue());
                    }
                    redisTemplate.opsForHash().putAll(redisKey, redisHashData);
                    log.info("Loaded {} role views into cache for orgId {}", redisHashData.size(), orgId);
                } catch (Exception redisEx) {
                    log.error("Redis cache update failed for members of orgId {}. Error: {}", orgId, redisEx.getMessage(), redisEx);
                }
            } else {
                log.warn("RedisTemplate is null. Skipping cache write for key: {}", redisKey);
            }
            return CompletableFuture.completedFuture(roleWiseUserMap);
        }catch (Exception e) {
            log.error("Error during loadAllUsers for orgId={}", orgId, e);
            throw new RuntimeException("Failed to cache member data", e);
        }
    }

    /**
     *
     * @param orgId
     * @return user profile of all the user with in the organization and cache it in redis cache key.
     */

    public CompletableFuture<Map<String, UserProfileResponseDto>> loadUsersProfile(String orgId, String schema) {

        log.info("Current User profile tenant:{}", schema);
        List<UserEntity> users = userRepository.findAllActiveUsersByOrganizationId(orgId);
        Map<String, UserProfileResponseDto> userProfileMap = new HashMap<>();

        if (users.isEmpty()) {
            log.warn("No active users found for orgId: {}", orgId);
            return CompletableFuture.completedFuture(userProfileMap);
        }

        // Fetch organization + type
        OrganizationEntity org = organizationRepository.findByOrganizationId(orgId).orElse(null);
        Optional<OrganizationTypeEntity> organizationType =
                (org != null && org.getOrgType() != null)
                        ? organizationTypeRepository.findById(org.getOrgType())
                        : Optional.empty();

        for (UserEntity user : users) {
            String userId = user.getUserId();

            // Fetch locations
            List<UserLocationEntity> userLocations = userLocationRepository.findByUser_UserId(userId);
            if (userLocations.isEmpty()) {
                log.warn("Skipping userId {} due to no locations", userId);
                continue;
            }

            List<LocationDto> locationDtos = userLocations.stream()
                    .filter(ul -> ul.getLocation() != null)
                    .map(ul -> locationDtoMapper.toDto(ul.getLocation()))
                    .toList();

            // Fetch groups
            List<UserGroupEntity> userGroups = userRepository.findUserByOrganizationIdAndUserId(orgId, userId);
            List<UserGroupProfileDto> groupDtos = userGroups.isEmpty()
                    ? Collections.emptyList()
                    : userGroups.stream().map(userDtoMapper::toGroupsDto).toList();

            // Parent details if student
            AtomicReference<List<ParentDto>> parentDto = new AtomicReference<>(Collections.emptyList());

            if (UserRole.STUDENT.name().equalsIgnoreCase(user.getRole().getName())) {
                userRepository.findByUserId(userId).ifPresent(sp -> {
                    secondaryDetailsRepository.findByUserId(sp.getUserId()).ifPresent(parentEntity -> {
                        parentDto.set(List.of(new ParentDto(
                                parentEntity.getId(),
                                parentEntity.getUserName(),
                                parentEntity.getEmail(),
                                parentEntity.getMobile()
                        )));
                    });
                });
            }

            // Build profile
            UserProfileResponseDto profile = new UserProfileResponseDto(
                    userId,
                    user.getUserName(),
                    user.getEmail(),
                    user.getMobileNumber(),
                    user.getRole().getName(),
                    user.getDateOfJoining(),
                    locationDtos,
                    groupDtos,
                    org != null ? org.getOrgName() : null,
                    (user.getWorkSchedule() != null ? user.getWorkSchedule().getScheduleName() : "-"),
                    organizationType.map(OrganizationTypeEntity::getOrgTypeName).orElse("-"),
                    parentDto.get()
            );

            userProfileMap.put(userId, profile);
        }

        // Cache all profiles
        try {
            if (redisTemplate != null) {
                String redisKey = cacheKeyUtil.getProfileKey(orgId, schema);
                redisTemplate.opsForHash().putAll(redisKey, userProfileMap);
                log.info("Cached {} profiles under key: {}", userProfileMap.size(), redisKey);
            } else {
                log.warn("RedisTemplate is null, skipping cache for user profiles for orgId: {}", orgId);
            }
        } catch (Exception e) {
            log.warn("Redis putAll failed for orgId {}: {}", orgId, e.getMessage());
        }

        return CompletableFuture.completedFuture(userProfileMap);
    }

    /**
     *
     * @param orgId
     * @return group based on organization from db and cache it redis cache using keys
     */
    public CompletableFuture<List<GroupResponseDto>> loadGroupsCache(String orgId, String schema) {

        log.info("Current Group tenant:{}", schema);
        List<GroupsData> groupRows = groupRepository.getGroupData(orgId);

        Map<Long, GroupResponseDto> groupMap = new HashMap<>();
        Map<String, Set<Long>> supervisorToGroupIds = new HashMap<>();
        Map<String, Set<String>> userToGroups = new HashMap<>();
        Map<String, Set<String>> userToLocations = new HashMap<>();
        Map<Long, List<UserGroupDto>> groupIdToActiveMembers = new HashMap<>();

        for (GroupsData row : groupRows) {
            Long groupId = row.getGroupId();
            String groupName = row.getGroupName();
            String location = row.getLocation();
            String workSchedule= row.getWorkSchedule();
            List<UserGroupDto> members = textUtil.parseMembers(row.getMembersDetails());
            List<UserGroupDto> activeMembers = members.stream().filter(m -> Boolean.TRUE.equals(m.getActive())).toList();

            groupIdToActiveMembers.put(groupId, activeMembers);

            for (UserGroupDto member : activeMembers) {
                String memberId = member.getUserId();
                userToGroups.computeIfAbsent(memberId, k -> new HashSet<>()).add(groupName);
                userToLocations.computeIfAbsent(memberId, k -> new HashSet<>()).add(location);
                if ("SUPERVISOR".equalsIgnoreCase(member.getType())) {
                    supervisorToGroupIds.computeIfAbsent(member.getUserId(), k -> new HashSet<>()).add(groupId);
                }
            }
        }

        for (Map.Entry<Long, List<UserGroupDto>> entry : groupIdToActiveMembers.entrySet()) {
            Long groupId = entry.getKey();
            List<UserGroupDto> enrichedMembers = entry.getValue();

            for (UserGroupDto member : enrichedMembers) {
                member.setGroupName(new ArrayList<>(userToGroups.getOrDefault(member.getUserId(), Set.of())));
                member.setLocation(new ArrayList<>(userToLocations.getOrDefault(member.getUserId(), Set.of())));
            }

            GroupsData row = groupRows.stream().filter(r -> r.getGroupId().equals(groupId)).findFirst().orElseThrow();
            groupMap.put(groupId, new GroupResponseDto(groupId, row.getGroupName(), row.getLocation(), row.getWorkSchedule(), enrichedMembers));
        }

        String orgGroupKey = cacheKeyUtil.getAllGroupsKey(orgId,schema);
        String supervisedMapKey = cacheKeyUtil.getSupervisedGroupsKey(orgId,schema);

        if (redisTemplate != null) {
            redisTemplate.delete(orgGroupKey);
            redisTemplate.opsForHash().putAll(orgGroupKey,
                    groupMap.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
            redisTemplate.delete(supervisedMapKey);
            for (Map.Entry<String, Set<Long>> entry : supervisorToGroupIds.entrySet()) {
                redisTemplate.opsForHash().put(supervisedMapKey, entry.getKey().toString(), entry.getValue());
            }
        } else {
            log.warn("RedisTemplate is null, skipping cache operations for keys: {}, {}", orgGroupKey, supervisedMapKey);
        }

        log.info("Group cache fully refreshed for orgId={}, groups={}, supervisors={}", orgId, groupMap.size(), supervisorToGroupIds.size());

        return CompletableFuture.completedFuture(new ArrayList<>(groupMap.values()));
    }

    public CompletableFuture<Map<String, List<UserResponseDto>>> loadAllInactiveUsers(String orgId, String schema) {

        log.info("Current Inactive User tenant:{}", schema);
        try {
            String redisKey = cacheKeyUtil.getInactiveMemberKey(orgId,schema);
            Map<String, List<UserResponseDto>> roleWiseUserMap = new HashMap<>();

            for (UserRole role : List.of(UserRole.values())) {
                int hierarchyLevel = UserRole.getLevel(String.valueOf(role));

                List<UserResponse> users = userRepository.findAllInActiveUsers(orgId, hierarchyLevel);
                if (users.isEmpty()) {
                    log.warn("No Inactive users found for orgId={} and role={}", orgId, role);
                    roleWiseUserMap.put(String.valueOf(role), new ArrayList<>());
                    continue;
                }

                // Convert to DTO
                List<UserResponseDto> usersDto = users.stream()
                        .map(userDtoMapper::toDto)
                        .toList();

                // Merge duplicates
                Map<String, UserResponseDto> userMap = new LinkedHashMap<>();
                for (UserResponseDto user : usersDto) {
                    userMap.compute(user.getUserId(), (id, existing) -> {
                        if (existing == null) return user;

                        if (!existing.getGroupName().contains(user.getGroupName().get(0))) {
                            existing.getGroupName().add(user.getGroupName().get(0));
                        }
                        if (!existing.getLocationName().contains(user.getLocationName().get(0))) {
                            existing.getLocationName().add(user.getLocationName().get(0));
                        }
                        return existing;
                    });
                }

                // Add merged list for current role
                roleWiseUserMap.put(String.valueOf(role), new ArrayList<>(userMap.values()));
            }

            // Final Step: Store everything in a single Redis Hash
            try {
                if (redisTemplate != null) {
                    redisTemplate.delete(redisKey);
                    Map<String, Object> redisHashData = new HashMap<>();
                    for (Map.Entry<String, List<UserResponseDto>> entry : roleWiseUserMap.entrySet()) {
                        redisHashData.put(entry.getKey().toLowerCase(), entry.getValue());
                    }
                    redisTemplate.opsForHash().putAll(redisKey, redisHashData);
                    log.info("Loaded {} role views into cache for Inactive user for orgId {}", redisHashData.size(), orgId);
                } else {
                    log.warn("RedisTemplate is null, skipping cache operations for Inactive User key: {}", redisKey);
                }
            } catch (Exception redisEx) {
                log.error("Redis cache update failed for members: {}", redisEx.getMessage(), redisEx);
            }

            return CompletableFuture.completedFuture(roleWiseUserMap);

        } catch (Exception e) {
            log.error("Error during loadAllRoleUsers for orgId={}", orgId, e);
            throw new RuntimeException("Failed to cache member data", e);
        }
    }
}
