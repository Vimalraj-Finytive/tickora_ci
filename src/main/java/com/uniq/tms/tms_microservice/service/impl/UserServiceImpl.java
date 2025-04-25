package com.uniq.tms.tms_microservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.AddGroupDto;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.GroupResponseDto;
import com.uniq.tms.tms_microservice.dto.UserGroupDto;
import com.uniq.tms.tms_microservice.dto.UserRole;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.AddGroup;
import com.uniq.tms.tms_microservice.model.AddMember;
import com.uniq.tms.tms_microservice.model.Group;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.model.User;
import com.uniq.tms.tms_microservice.model.UserGroup;
import com.uniq.tms.tms_microservice.model.UserResponse;
import com.uniq.tms.tms_microservice.repository.LocationRepository;
import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.service.UserService;
import com.uniq.tms.tms_microservice.util.EmailUtil;
import com.uniq.tms.tms_microservice.util.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserAdapter userAdapter;
    private final TimesheetAdapter timesheetAdapter;
    private final UserEntityMapper userEntityMapper;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;
    private final EmailUtil emailUtil;
    private final UserDtoMapper userDtoMapper;
    private final ObjectMapper objectMapper;

    public UserServiceImpl(UserAdapter userAdapter, TimesheetAdapter timesheetAdapter, UserEntityMapper userEntityMapper, OrganizationRepository organizationRepository, RoleRepository roleRepository, LocationRepository locationRepository, EmailUtil emailUtil, UserDtoMapper userDtoMapper, ObjectMapper objectMapper) {
        this.userAdapter = userAdapter;
        this.timesheetAdapter = timesheetAdapter;
        this.userEntityMapper = userEntityMapper;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.emailUtil = emailUtil;
        this.userDtoMapper = userDtoMapper;
        this.objectMapper = objectMapper;
    }

    private static final  Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public List<Role> getAllRole(Long orgId, String role) {

        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        int hierarchyLevel = UserRole.getLevel(role);
        List<Role> roles = userAdapter.getAllRole(orgId, hierarchyLevel)
                .stream()
                .map(userEntityMapper::toMiddleware)
                .toList();
        return roles;
    }

    @Override
    public List<Group> getAllTeam() {
        List<Group> teams = userAdapter.getAllTeams().stream().map(userEntityMapper::toMiddleware).toList();
        return teams;
    }

    @Override
    public List<Location> getAllLocation(Long orgId) {
        List<Location> location = userAdapter.getAllLocation(orgId).stream().map(userEntityMapper::toMiddleware).toList();
        return location;
    }

    @Override
    public User createUser(User usermiddleware, Long organizationId) {

        if (userAdapter.existsByEmail(usermiddleware.getEmail())) {
            throw new DataIntegrityViolationException("User with email already exists");
        }

        if(userAdapter.existsByMobileNumber(usermiddleware.getMobileNumber())){
            throw new DataIntegrityViolationException( "User with mobile number already exists");
        }

        UserEntity entity = userEntityMapper.toEntity(usermiddleware);
        entity.setOrganizationId(organizationId);

        if (usermiddleware.getRoleId() == null) {
            throw new IllegalArgumentException("roleId must not be null");
        }

        RoleEntity role = roleRepository.findById(usermiddleware.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + usermiddleware.getRoleId()));

        entity.setRole(role);

        String defaultPassword = PasswordUtil.generateDefaultPassword();
        String encryptedPassword = PasswordUtil.encryptPassword(defaultPassword);
        entity.setPassword(encryptedPassword);
        entity.setDefaultPassword(true);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());

        UserEntity saveEntity = userAdapter.saveUser(entity);
        boolean isNewUser = saveEntity.isDefaultPassword();
        emailUtil.sendAccountCreationEmail(usermiddleware.getEmail(), usermiddleware.getUserName(), defaultPassword, isNewUser);

        return userEntityMapper.toMiddleware(saveEntity);
    }

    @Override
    public User updateUser(Map<String, Object> updates, Long orgId, Long userId) {

        UserEntity existingUser = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingUser.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized");
        }

        updates.forEach((key, value) -> {
            if ("roleId".equals(key)) {
                existingUser.setRole(roleRepository.findById(Long.parseLong(value.toString()))
                        .orElseThrow(() -> new RuntimeException("Role not found")));
            } else {
                setField(existingUser, key, value);
            }
        });

        return userEntityMapper.toMiddleware(userAdapter.updateUser(existingUser));
    }

    private void setField(UserEntity user, String key, Object value) {
        try {
            Field field = UserEntity.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(user, convertValue(field.getType(), value));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error updating field: " + key, e);
        }
    }

    private Object convertValue(Class<?> fieldType, Object value) {
        if (value == null) {
            return null;
        }
        if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
            return Long.parseLong(value.toString());
        }
        if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
            return Integer.parseInt(value.toString());
        }
        if (fieldType.equals(Double.class)) {
            return Double.parseDouble(value.toString());
        }
        if (fieldType.equals(LocalDate.class)) {
            return LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return value;
    }

    @Override
    public List<UserResponse> getUsers(Long orgId, String role) {

        int hierarchyLevel = UserRole.getLevel(role);
        List<UserResponse> users = userAdapter.findByOrganizationId(orgId, hierarchyLevel);
        if (users.isEmpty()) {
            throw new RuntimeException("Unauthorized");
        }
        Map<Long, UserResponse> userMap = new LinkedHashMap<>();
        for (UserResponse row : users) {
            userMap.compute(row.getUserId(), (id, existing) -> {
                if (existing == null) {
                    return new UserResponse(
                            row.getUserId(),
                            row.getUserName(),
                            row.getEmail(),
                            row.getMobileNumber(),
                            row.getGroupName(),
                            row.getRoleName(),
                            row.getLocationName(),
                            row.getDateOfJoining()
                    );
                } else {
                    // Merge group names (if not already present)
                    String mergedGroups = existing.getGroupName();
                    if (!mergedGroups.contains(row.getGroupName())) {
                        mergedGroups += ", " + row.getGroupName();
                    }
                    existing.setGroupName(mergedGroups);
                    return existing;
                }
            });
        }
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User deleteUser(Long orgId, Long userId) {
        UserEntity user = userAdapter.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User ID not found."));

        if (!user.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized");
        }
        userAdapter.deactivateUserById(userId);
        return userEntityMapper.toMiddleware(user);
    }

    @Override
    public AddGroup createGroup(AddGroup groupMiddleware, Long orgId) {
        if (userAdapter.findByGroup(groupMiddleware.getGroupName(), orgId)) {
            throw new DataIntegrityViolationException("Group '" + groupMiddleware.getGroupName() + "' already exists in this organization");
        }

        GroupEntity entity = userEntityMapper.toEntity(groupMiddleware);

        OrganizationEntity orgEntity = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + orgId));
        entity.setOrganizationEntity(orgEntity);

        if (groupMiddleware.getLocationId() != null) {
            LocationEntity locationEntity = locationRepository.findById(groupMiddleware.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + groupMiddleware.getLocationId()));
            entity.setLocationEntity(locationEntity);
        }

        if (groupMiddleware.getWorkScheduleId() != null) {
            WorkScheduleEntity ws = userAdapter.findByWorkscheduleId(groupMiddleware.getWorkScheduleId());
            entity.setWorkSchedule(ws);
        } else {
            WorkScheduleEntity defaultWs = userAdapter.findDefaultActiveSchedule();
            entity.setWorkSchedule(defaultWs);
        }

        GroupEntity savedEntity = userAdapter.saveGroup(entity);

        for (Long id : groupMiddleware.getSupervisorsId()) {
            UserEntity user = userAdapter.findById(id).orElseThrow(()->new UsernameNotFoundException("User ID " + id + " not found."));

            createUserGroup(new UserGroup(savedEntity.getGroupId(), id, groupMiddleware.getType()),orgId);

        }
        return userEntityMapper.toGroupMiddleware(savedEntity);
    }

    @Override
    public List<User> addUserToGroup(AddMember addMemberMiddleware, Long orgId){
        List<User> savedUsers =new ArrayList();
        for (Long id : addMemberMiddleware.getUserId()) {
            UserEntity userEntity = userAdapter.findById(id).orElseThrow(()->new UsernameNotFoundException("User not found."));
            createUserGroup(new UserGroup(addMemberMiddleware.getGroupId(), id, addMemberMiddleware.getType()), orgId);
            User userMiddleware = userEntityMapper.toMiddleware(userEntity);
            savedUsers.add(userMiddleware);
        }
        return savedUsers;
    }

    @Override
    public UserGroup createUserGroup(UserGroup userGroupMiddleware, Long orgId) {
        List<UserGroupEntity> existing = userAdapter.findByUserIdAndGroupId(
                userGroupMiddleware.getUserId(),
                userGroupMiddleware.getGroupId()
        );
        if (!existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user is already assigned to this group more than once.");
        }

        UserGroupEntity entity = userEntityMapper.toEntity(userGroupMiddleware);
        UserGroupEntity savedEntity=null;
        savedEntity = userAdapter.saveUserGroup(entity);

        return userEntityMapper.toMiddleware(savedEntity);
    }

    @Override
    public void updateGroupDetails(AddGroupDto addGroupDto, Long groupId, Long orgId){
        AddGroup addGroup = userDtoMapper.toMiddleware(addGroupDto);
        GroupEntity groupEntity = userEntityMapper.toEntity(addGroup);
        boolean nameExists = userAdapter.existsGroupNameInOrganization(groupEntity.getGroupName(),orgId, groupId);

        if (nameExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Group name already exists");
        }

        userAdapter.updateGroupNameAndLocation(groupId,groupEntity.getGroupName(),groupEntity.getLocationEntity().getLocationId());

        // Step 2: Insert new supervisors
        GroupEntity group = new GroupEntity(groupId);
        for (Long usersId : addGroup.getSupervisorsId()) {
            UserEntity user = new UserEntity(usersId);
            UserGroupEntity supervisorEntry = new UserGroupEntity();
            supervisorEntry.setGroup(group);
            supervisorEntry.setUser(user);
            supervisorEntry.setType("Supervisor");
            userAdapter.deleteSupervisorsByGroupId(groupId, usersId);

            userAdapter.saveUserGroup(supervisorEntry);
        }
    }

    public List<GroupResponseDto> getAllGroups(Long orgId) throws JsonProcessingException {
        List<Object[]> results = userAdapter.getGroupData(orgId);

        // Maps for collecting union data
        Map<Long, Set<String>> userToGroups = new HashMap<>();
        Map<Long, Set<String>> userToLocations = new HashMap<>();
        Map<Long, List<UserGroupDto>> groupIdToActiveMembers = new HashMap<>();

        for (Object[] row : results) {
            Long groupId = ((Number) row[0]).longValue();
            String groupName = (String) row[1];
            String location = (String) row[2];

            if (row[3] != null) {
                String json = row[3].toString();
                List<UserGroupDto> allMembers = objectMapper.readValue(json, new TypeReference<>() {});

                for (UserGroupDto member : allMembers) {
                    if (Boolean.TRUE.equals(member.getActive())) {
                        Long userId = member.getUserId();

                        // Collect unions
                        userToGroups.computeIfAbsent(userId, k -> new HashSet<>()).add(groupName);
                        userToLocations.computeIfAbsent(userId, k -> new HashSet<>()).add(location);

                        // Assign member to group
                        groupIdToActiveMembers.computeIfAbsent(groupId, k -> new ArrayList<>()).add(member);
                    }
                }
            }
        }

        // Final transformation
        List<GroupResponseDto> finalList = new ArrayList<>();

        for (Object[] row : results) {
            Long groupId = ((Number) row[0]).longValue();
            String groupName = (String) row[1];
            String location = (String) row[2];

            List<UserGroupDto> members = groupIdToActiveMembers.get(groupId);
            if (members == null || members.isEmpty()) {
                log.info("Group {} skipped because it has no active members", groupId);
                continue;
            }

            // Enrich members with union info
            for (UserGroupDto member : members) {
                member.setGroupName(new ArrayList<>(userToGroups.getOrDefault(member.getUserId(), Set.of())));
                member.setLocation(new ArrayList<>(userToLocations.getOrDefault(member.getUserId(), Set.of())));
            }

            finalList.add(new GroupResponseDto(groupId, groupName, location, members));
        }
        return finalList;
    }

    @Override
    public boolean updateUserGroupType(UserGroup userGroup) {
        // Map of valid prefixes to their corresponding roles
        Map<String, String> typeMap = Map.of(
                "m", "Member",
                "s", "Supervisor"
        );

        String type = typeMap.entrySet().stream()
                .filter(entry -> userGroup.getType().toLowerCase().startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid type. Must start with 'm' or 's'."));

        int updatedCount = userAdapter.updateUserGroupType(userGroup.getUserId(),userGroup.getGroupId(),type);
        return updatedCount > 0;
    }

    @Override
    public void deleteMember(Long groupId, Long memberId) {
        userAdapter.deleteMember(groupId, memberId);
    }

    @Override
    public void deleteGroup(Long groupId) {
        userAdapter.deleteByGroupId(groupId);
        userAdapter.deleteGroup(groupId);
    }

    @Override
    public List<User> getMembers(Long orgId, Long roleId) {
        // if roleId is present, get users by roleId
        if (roleId != null) {
            return userAdapter.getMembers(orgId, roleId).stream()
                    .map(userEntityMapper::toMiddleware).toList();
        } else {
            // Get the hierarchy level of the "Student" role dynamically
            int studentHierarchyLevel = UserRole.STUDENT.getHierarchyLevel();
            // Get all roles above the "Student" role hierarchy level
            List<UserRole> higherRoles = Arrays.stream(UserRole.values())
                    .filter(r -> r.getHierarchyLevel() < studentHierarchyLevel)
                    .toList();
            List<Integer> higherRoleIds = higherRoles.stream()
                    .map(roles-> roles.getHierarchyLevel())
                    .toList();
           return userAdapter.getMembersByRole(orgId, higherRoleIds).stream()
                   .map(userEntityMapper::toMiddleware).toList();
        }
    }

    @Override
    public List<GroupDto> getUserGroups(Long userId, Long orgId) {
        if(userId == null){
            List<GroupDto> Allgroup = userAdapter.getAllgroups(orgId);
            return Allgroup;
        }
        List<GroupDto> group = userAdapter.getUserGroups(userId, orgId);
        return group;
    }

    @Override
    public List<Map<String, Object>> getGroupMembers(Long groupId, Long orgId, LocalDate date) {

        List<UserGroupEntity> groupEntity = userAdapter.getGroupMembersByGroupId(groupId, orgId);
        List<Long> memberIds = groupEntity.stream()
                .map(ug -> ug.getUser().getUserId())
                .toList();

        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        //Fetch list of users in a group
        List<UserEntity> GroupUsers = userAdapter.getUsersByIds(memberIds, orgId);

        List<TimesheetEntity> latestLogs = timesheetAdapter.
                getLatestLogsByTimesheetIds(memberIds, orgId, date);
        Map<Long, TimesheetEntity> latestLogsMap = latestLogs.stream()
                .collect(Collectors.toMap(TimesheetEntity::getUserId, Function.identity()));

        List<Map<String, Object>> UserDetailsList = GroupUsers.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getUserId());
                    map.put("name", user.getUserName());
                    map.put("role", user.getRole().getName());

                    TimesheetEntity timesheet = latestLogsMap.get(user.getUserId());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

                    if (timesheet != null) {
                        LocalTime clockIn = timesheet.getFirstClockIn();
                        LocalTime clockOut = timesheet.getLastClockOut();

                        map.put("firstClockIn", clockIn != null ? clockIn.format(formatter) : null);
                        map.put("lastClockOut", clockOut != null ? clockOut.format(formatter) : null);
                    } else {
                        map.put("firstClockIn", null);
                        map.put("lastClockOut", null);
                    }
                    return map;
                })
                .toList();
                return UserDetailsList;
    }
}
