package com.uniq.tms.tms_microservice.modules.userManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.mapper.LocationDtoMapper;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserProjection;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.shared.util.TextUtil;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.GroupsData;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.GroupRepository;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.UserLocationRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserCacheService;
import com.uniq.tms.tms_microservice.shared.util.UserMergeUtil;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserCacheServiceImpl implements UserCacheService {

    private final Map<PrivilegeConstants, String> privilegeMap = new ConcurrentHashMap<>();

    private static final Logger log = LogManager.getLogger(UserCacheServiceImpl.class);

    private final UserRepository userRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserDtoMapper userDtoMapper;
    private final GroupRepository groupRepository;
    private final TextUtil textUtil;
    private final CacheKeyUtil cacheKeyUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocationDtoMapper locationDtoMapper;
    private final UserMergeUtil userMergeUtil;

    public UserCacheServiceImpl(UserRepository userRepository,
                                UserLocationRepository userLocationRepository, UserDtoMapper userDtoMapper,
                                GroupRepository groupRepository, TextUtil textUtil,
                                CacheKeyUtil cacheKeyUtil, @Nullable RedisTemplate<String, Object> redisTemplate, LocationDtoMapper locationDtoMapper, UserMergeUtil userMergeUtil) {
        this.userRepository = userRepository;
        this.userLocationRepository = userLocationRepository;
        this.userDtoMapper = userDtoMapper;
        this.groupRepository = groupRepository;
        this.textUtil = textUtil;
        this.cacheKeyUtil = cacheKeyUtil;
        this.redisTemplate = redisTemplate;
        this.locationDtoMapper = locationDtoMapper;
        this.userMergeUtil = userMergeUtil;
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
            for (UserRole role : UserRole.values()) {
                int hierarchyLevel = UserRole.getLevel(role.name());
                List<UserProjection> users = userRepository.findAllUsers(orgId, hierarchyLevel);
                if (users.isEmpty()) {
                    log.warn("No users found for orgId={} and role={}", orgId, role);
                    continue;
                }
                Map<String, UserResponseDto> mergedUserMap = userMergeUtil.mergeUserRecords(users);
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

    @Override
    public CompletableFuture<Map<String, UserProfileResponseDto>> loadUsersProfile(String orgId, String schema) {

        List<UserProjection> rows = userRepository.findAllUsers();
        Map<String, UserResponseDto> merged = userMergeUtil.mergeUserRecords(rows);

        Map<String, UserProfileResponseDto> userProfileMap = new HashMap<>();

        for (UserResponseDto user : merged.values()) {

            List<UserLocationEntity> userLocations = userLocationRepository.findByUser_UserId(user.getUserId());
            List<LocationDto> locationDtos;

            if (userLocations.isEmpty()) {
                log.warn("No locations found for userId {}", user.getUserId());
                locationDtos = Collections.emptyList();
            } else {
                locationDtos = userLocations.stream()
                        .filter(ul -> ul.getLocation() != null)
                        .map(ul -> locationDtoMapper.toDto(ul.getLocation()))
                        .toList();
            }

            List<UserGroupEntity> userGroups = userRepository.findUserByOrganizationIdAndUserId(orgId, user.getUserId());
            List<UserGroupProfileDto> groupDtos = userGroups.isEmpty()
                    ? Collections.emptyList()
                    : userGroups.stream().map(userDtoMapper::toGroupsDto).toList();

            List<UserPolicyDto> singlePolicy =
                    (user.getPolicies() != null && !user.getPolicies().isEmpty())
                            ? user.getPolicies()
                            : null;

            List<ParentDto> parentList = new ArrayList<>();
            if (user.getSecondaryDetails() != null) {
                var sec = user.getSecondaryDetails();
                parentList.add(new ParentDto(
                        null,
                        sec.getUserName(),
                        sec.getEmail(),
                        sec.getMobile()
                ));
            }

            UserProfileResponseDto dto = new UserProfileResponseDto(
                    user.getUserId(),
                    user.getUserName(),
                    user.getEmail(),
                    user.getMobileNumber(),
                    user.getRoleName(),
                    user.getDateOfJoining(),
                    locationDtos,
                    groupDtos,
                    user.getOrganizationName() != null ? user.getOrganizationName() : "-",
                    user.getScheduleName(),
                    user.getOrgType() != null ? user.getOrgType() : "-",
                    user.getCalendarName(),
                    user.getRequestApproverName(),
                    user.getPayrollName(),
                    singlePolicy,
                    parentList
            );

            userProfileMap.put(user.getUserId(), dto);
        }
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

    @Override
    public CompletableFuture<Map<String, List<UserResponseDto>>> loadAllInactiveUsers(
            String orgId,
            String schema
    ) {

        log.info("Current Inactive User tenant: {}", schema);

        try {
            String redisKey = cacheKeyUtil.getInactiveMemberKey(orgId, schema);
            Map<String, List<UserResponseDto>> roleWiseUserMap = new HashMap<>();

            for (UserRole role : UserRole.values()) {

                int hierarchyLevel = UserRole.getLevel(role.name());

                List<UserProjection> users =
                        userRepository.findAllInActiveUsers(orgId, hierarchyLevel);

                if (users == null || users.isEmpty()) {
                    log.warn("No inactive users found for orgId={} and role={}", orgId, role);
                    roleWiseUserMap.put(role.name(), new ArrayList<>());
                    continue;
                }

                List<UserResponseDto> usersDto =
                        users.stream()
                                .map(userDtoMapper::toUserDto)
                                .filter(dto -> dto.getUserId() != null)
                                .toList();

                Map<String, List<UserResponseDto>> groupedByUser =
                        usersDto.stream()
                                .collect(Collectors.groupingBy(UserResponseDto::getUserId));

                Map<String, UserResponseDto> mergedUsers = new LinkedHashMap<>();

                for (Map.Entry<String, List<UserResponseDto>> entry : groupedByUser.entrySet()) {

                    String userId = entry.getKey();
                    List<UserResponseDto> rows = entry.getValue();

                    UserResponseDto merged = createBaseInactiveUser(rows.getFirst());

                    for (UserResponseDto row : rows) {
                        mergeGroupNames(merged, row);
                        mergeLocationNames(merged, row);
                        mergePolicies(merged, row);
                    }

                    mergedUsers.put(userId, merged);
                }

                roleWiseUserMap.put(role.name(), new ArrayList<>(mergedUsers.values()));
            }

            try {
                if (redisTemplate != null) {
                    redisTemplate.delete(redisKey);

                    Map<String, Object> redisHashData = new HashMap<>();
                    for (Map.Entry<String, List<UserResponseDto>> entry : roleWiseUserMap.entrySet()) {
                        redisHashData.put(entry.getKey().toLowerCase(), entry.getValue());
                    }

                    redisTemplate.opsForHash().putAll(redisKey, redisHashData);

                    log.info(
                            "Loaded {} role views into inactive user cache for orgId {}",
                            redisHashData.size(),
                            orgId
                    );
                } else {
                    log.warn("RedisTemplate is null. Skipping inactive user cache write.");
                }
            } catch (Exception redisEx) {
                log.error("Redis cache update failed for inactive users", redisEx);
            }

            return CompletableFuture.completedFuture(roleWiseUserMap);

        } catch (Exception e) {
            log.error("Error during loadAllInactiveUsers for orgId={}", orgId, e);
            throw new RuntimeException("Failed to cache member data", e);
        }
    }

    private UserResponseDto createBaseInactiveUser(UserResponseDto source) {

        UserResponseDto dto = new UserResponseDto(source);

        dto.setGroupName(
                source.getGroupName() != null
                        ? new ArrayList<>(source.getGroupName())
                        : new ArrayList<>()
        );

        dto.setLocationName(
                source.getLocationName() != null
                        ? new ArrayList<>(source.getLocationName())
                        : new ArrayList<>()
        );

        dto.setPolicies(
                source.getPolicies() != null
                        ? new ArrayList<>(source.getPolicies())
                        : new ArrayList<>()
        );

        return dto;
    }

    private void mergeGroupNames(UserResponseDto target, UserResponseDto source) {

        if (source.getGroupName() == null || source.getGroupName().isEmpty()) return;

        String group = source.getGroupName().getFirst();

        if (!target.getGroupName().contains(group)) {
            target.getGroupName().add(group);
        }
    }

    private void mergeLocationNames(UserResponseDto target, UserResponseDto source) {

        if (source.getLocationName() == null || source.getLocationName().isEmpty()) return;

        String location = source.getLocationName().getFirst();

        if (!target.getLocationName().contains(location)) {
            target.getLocationName().add(location);
        }
    }

    private void mergePolicies(UserResponseDto target, UserResponseDto source) {

        if (source.getPolicies() == null || source.getPolicies().isEmpty()) return;

        var policy = source.getPolicies().getFirst();

        boolean exists =
                target.getPolicies()
                        .stream()
                        .anyMatch(p -> p.getPolicyName().equals(policy.getPolicyName()));

        if (!exists) {
            target.getPolicies().add(policy);
        }
    }

}
