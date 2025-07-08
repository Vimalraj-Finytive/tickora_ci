package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.mapper.LocationDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.UserResponse;
import com.uniq.tms.tms_microservice.repository.*;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.util.TextUtil;
import jakarta.annotation.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class CacheLoaderServiceImpl implements CacheLoaderService {

    private final Map<PrivilegeConstants, String> privilegeMap = new ConcurrentHashMap<>();

    private static final Logger log = LogManager.getLogger(CacheLoaderServiceImpl.class);
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final LocationDtoMapper locationDtoMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final CacheKeyUtil cacheKeyUtil;
    private final OrganizationRepository organizationRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserDtoMapper userDtoMapper;
    private final TeamRepository teamRepository;
    private final TextUtil textUtil;

    public CacheLoaderServiceImpl(UserRepository userRepository, LocationRepository locationRepository, LocationDtoMapper locationDtoMapper, @Nullable RedisTemplate<String, Object> redisTemplate, RoleRepository roleRepository, PrivilegeRepository privilegeRepository, CacheKeyUtil cacheKeyUtil, OrganizationRepository organizationRepository, UserLocationRepository userLocationRepository, UserDtoMapper userDtoMapper, TeamRepository teamRepository, TextUtil textUtil) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.locationDtoMapper = locationDtoMapper;
        this.redisTemplate = redisTemplate;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.cacheKeyUtil = cacheKeyUtil;
        this.organizationRepository = organizationRepository;
        this.userLocationRepository = userLocationRepository;
        this.userDtoMapper = userDtoMapper;
        this.teamRepository = teamRepository;
        this.textUtil = textUtil;
    }

    LocalDateTime now = LocalDateTime.now();

    /**
     *
     * @param orgId
     * @return All user list based on logged user hierarchy Level and organization and caches in redis cache key
     */
    public CompletableFuture<Map<String, List<UserResponseDto>>> loadAllUsers(Long orgId) {
        try {
            String redisKey = cacheKeyUtil.getMemberKey(orgId); // e.g., members:org:1
            Map<String, List<UserResponseDto>> roleWiseUserMap = new HashMap<>();

            for (UserRole role : List.of(UserRole.values())) {
                int hierarchyLevel = UserRole.getLevel(String.valueOf(role));

                List<UserResponse> users = userRepository.findAllUsers(orgId, hierarchyLevel);
                if (users.isEmpty()) {
                    log.warn("No users found for orgId={} and role={}", orgId, role);
                    roleWiseUserMap.put(String.valueOf(role), Collections.emptyList());
                    continue;
                }

                // Convert to DTO
                List<UserResponseDto> usersDto = users.stream()
                        .map(userDtoMapper::toDto)
                        .toList();

                // Merge duplicates
                Map<Long, UserResponseDto> userMap = new LinkedHashMap<>();
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
                    log.info("Loaded {} role views into cache for orgId {}", redisHashData.size(), orgId);
                } else {
                    log.warn("RedisTemplate is null, skipping cache operations for key: {}", redisKey);
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

    /**
     *
     * @param orgId
     * @return location based on organization from db and cache it redis cache using keys
     */
    public CompletableFuture<List<Location>> loadLocationTable(Long orgId) {
        String redisKey = cacheKeyUtil.getLocationKey(orgId);

        try{
            List<Location> locations = locationRepository.findByOrgId(orgId).stream()
                    .map(locationDtoMapper::toLocationDTO)
                    .toList();
            if (!locations.isEmpty()) {
                try {
                    if (redisTemplate != null) {
                        CachedData<Location> locationCache = new CachedData<>(locations, now);
                        redisTemplate.opsForValue().set(redisKey, locationCache);
                        log.info("Locations table loaded into cache");
                    } else {
                        log.warn("RedisTemplate is null, skipping cache write for key: {}", redisKey);
                    }
                } catch (Exception e) {
                    log.warn("Redis not available, skipping cache write: {}", e.getMessage());
                }
            }
            else{
                log.warn("No locations found in DB while loading to cache.");
            }
            log.info("Locations return from DB");
            return CompletableFuture.completedFuture(locations);
        } catch (Exception e) {
            log.error("Failed to load locations from DB: ", e);
            throw new RuntimeException("DB fetch failed", e);
        }
    }

    public void loadAllRolesToCache(Long orgId) {
       List<RoleEntity> roles = roleRepository.findAllWithPrivileges();

        Map<String, Set<String>> rolePrivileges = new HashMap<>();
        for (RoleEntity role : roles) {
            Set<String> privileges = role.getPrivilegeEntities()
                    .stream()
                    .map(PrivilegeEntity::getName)
                    .collect(Collectors.toSet());
            rolePrivileges.put(role.getName(), privileges);
        }

        String redisKey = cacheKeyUtil.getRoleKey();
        if (redisTemplate != null) {
            rolePrivileges.forEach((role, privileges) -> {
                redisTemplate.opsForValue().set(redisKey + role.toLowerCase(), privileges);
                log.info("Loaded role {} with privileges: {}", role, privileges);
            });
        } else {
            log.warn("RedisTemplate is null, skipping cache for roles.");
        }
    }

    public String getPrivilegeKey(PrivilegeConstants constant) {
        return privilegeMap.get(constant);
    }

    public void loadPrivilegesFromDB(Long orgId) {
        List<PrivilegeEntity> privileges = privilegeRepository.findAll();
        log.info("Loading privileges from DB: {}", privileges.size());

        for (PrivilegeConstants constant : PrivilegeConstants.values()) {
            log.info("Loading privilege from Enum: {}", constant.name());
            privileges.stream()
                    .filter(p -> p.getStaticName().equalsIgnoreCase(constant.name()))
                    .findFirst()
                    .ifPresentOrElse(
                            p -> privilegeMap.put(constant, p.getName()),
                            () -> log.warn("Privilege NOT FOUND in DB for constant: {}", constant.name())
                    );
        }

        // Log all mapped privileges
        privilegeMap.forEach((key, value) ->
                log.info("Privilege constant: {}, Privilege name: {}", key, value));
    }

    /**
     *
     * @param orgId
     * @return user profile of all the user with in the organization and cache it in redis cache key.
     */
    public CompletableFuture<Map<String, UserProfileResponse>> loadUsersProfile(Long orgId) {
        List<UserEntity> users = userRepository.findAllActiveUsersByorganizationId(orgId);
        Map<String, UserProfileResponse> userProfileMap = new HashMap<>();

        if (users.isEmpty()) {
            log.warn("No active users found for orgId: {}", orgId);
            return CompletableFuture.completedFuture(userProfileMap);
        }

        OrganizationEntity org = organizationRepository.findById(orgId).orElse(null);

        for (UserEntity user : users) {
            Long userId = user.getUserId();

            // Fetch groups
            List<UserGroupEntity> userGroups = userRepository.findUserByOrganizationIdAndUserId(orgId, userId);

            // Fetch locations
            List<UserLocationEntity> userLocations = userLocationRepository.findByUser_UserId(userId);
            if (userLocations.isEmpty()) continue;

            List<LocationDto> locationDtos = userLocations.stream()
                    .filter(ul -> ul.getLocation() != null)
                    .map(ul -> userDtoMapper.toDto(ul.getLocation()))
                    .toList();

            List<UserGroupProfileDto> groupDtos = userGroups.isEmpty()
                    ? Collections.emptyList()
                    : userGroups.stream().map(userDtoMapper::toGroupsDto).toList();

            UserProfileResponse profile = new UserProfileResponse(
                    userId,
                    user.getUserName(),
                    user.getEmail(),
                    user.getMobileNumber(),
                    user.getRole().getName(),
                    user.getDateOfJoining(),
                    locationDtos,
                    groupDtos,
                    org != null ? org.getOrgName() : null
            );

            userProfileMap.put(userId.toString(), profile);
        }

        // Cache all profiles
        try {
            if (redisTemplate != null) {
                String redisKey = cacheKeyUtil.getprofileKey(orgId);
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

    public CompletableFuture<List<GroupResponseDto>> loadGroupsCache(Long orgId) {
        List<GroupsData> groupRows = teamRepository.getGroupData(orgId);

        Map<Long, GroupResponseDto> groupMap = new HashMap<>();
        Map<Long, Set<Long>> supervisorToGroupIds = new HashMap<>();
        Map<Long, Set<String>> userToGroups = new HashMap<>();
        Map<Long, Set<String>> userToLocations = new HashMap<>();
        Map<Long, List<UserGroupDto>> groupIdToActiveMembers = new HashMap<>();

        for (GroupsData row : groupRows) {
            Long groupId = row.getGroupId();
            String groupName = row.getGroupName();
            String location = row.getLocation();
            List<UserGroupDto> members = textUtil.parseMembers(row.getMembersDetails());
            List<UserGroupDto> activeMembers = members.stream().filter(m -> Boolean.TRUE.equals(m.getActive())).toList();

            groupIdToActiveMembers.put(groupId, activeMembers);

            for (UserGroupDto member : activeMembers) {
                Long memberId = member.getUserId();
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
            groupMap.put(groupId, new GroupResponseDto(groupId, row.getGroupName(), row.getLocation(), enrichedMembers));
        }

        String orgGroupKey = cacheKeyUtil.getAllGroupsKey(orgId);
        String supervisedMapKey = cacheKeyUtil.getSupervisedGroupsKey(orgId);

        if (redisTemplate != null) {
            redisTemplate.delete(orgGroupKey);
            redisTemplate.opsForHash().putAll(orgGroupKey,
                    groupMap.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
            redisTemplate.delete(supervisedMapKey);
            for (Map.Entry<Long, Set<Long>> entry : supervisorToGroupIds.entrySet()) {
                redisTemplate.opsForHash().put(supervisedMapKey, entry.getKey().toString(), entry.getValue());
            }
        } else {
            log.warn("RedisTemplate is null, skipping cache operations for keys: {}, {}", orgGroupKey, supervisedMapKey);
        }

        log.info("Group cache fully refreshed for orgId={}, groups={}, supervisors={}", orgId, groupMap.size(), supervisorToGroupIds.size());

        return CompletableFuture.completedFuture(new ArrayList<>(groupMap.values()));
    }
}
