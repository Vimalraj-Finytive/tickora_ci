package com.uniq.tms.tms_microservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.dto.UserRole;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.AddGroup;
import com.uniq.tms.tms_microservice.model.Group;
import com.uniq.tms.tms_microservice.model.GroupResponse;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.Member;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.model.User;
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
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.sql.Array;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserAdapter userAdapter;
    private final UserEntityMapper userEntityMapper;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;
    private final EmailUtil emailUtil;

    public UserServiceImpl(UserAdapter userAdapter, UserEntityMapper userEntityMapper,OrganizationRepository organizationRepository, RoleRepository roleRepository, LocationRepository locationRepository, EmailUtil emailUtil) {
        this.userAdapter = userAdapter;
        this.userEntityMapper = userEntityMapper;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.emailUtil = emailUtil;
    }

    @Override
    public List<Role> getAllRole(Long orgId, String role) {
        List<Role> roles = userAdapter.getAllRole(orgId, role)
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
            throw new DataIntegrityViolationException("User with email " + usermiddleware.getEmail() + " already exists");
        }

        UserEntity entity = userEntityMapper.toEntity(usermiddleware);
        entity.setOrganizationId(organizationId);

        System.out.println("Received roleId: " + usermiddleware.getRoleId());

        if (usermiddleware.getRoleId() == null) {
            throw new IllegalArgumentException("roleId must not be null");
        }

        RoleEntity role = roleRepository.findById(usermiddleware.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + usermiddleware.getRoleId()));

        entity.setRole(role);

        System.out.println("Assigned Role: " + entity.getRole().getName());

        String defaultPassword = PasswordUtil.generateDefaultPassword();
        String encryptedPassword = PasswordUtil.encryptPassword(defaultPassword);
        entity.setPassword(encryptedPassword);
        entity.setDefaultPassword(true);

        UserEntity saveEntity = userAdapter.saveUser(entity);
        boolean isNewUser = saveEntity.isDefaultPassword();
        System.out.println("Reset new user: " + isNewUser);
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
    public List<UserResponseDto> getUsers(Long orgId, String role) {
        System.out.println("Fetching users for orgId: " + orgId);
        List<String> accessibleRoles = UserRole.getRolesFor(role);
        if (accessibleRoles.isEmpty()) {
            throw new RuntimeException("Unauthorized");
        }
        List<UserResponseDto> users = userAdapter.findByOrganizationId(orgId, accessibleRoles);
        System.out.println("Fetched users: " + users);
        return users;
    }

    @Override
    public User deleteUser(Long orgId, Long userId) {
        UserEntity user = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized");
        }
        userAdapter.deleteUser(user);
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

        System.out.println("Saving Group: " + entity.getGroupName() + ", OrgID: " + orgId);

        GroupEntity savedEntity = userAdapter.saveGroup(entity);

        return userEntityMapper.toGroupMiddleware(savedEntity);
    }

    @Override
    public Member addUserToGroup(Long groupId, Member memberMiddleware, Long orgId) {
        GroupEntity groupEntity = userAdapter.findByTeamId(groupId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!groupEntity.getOrganizationEntity().getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized - You cannot modify this team");
        }

        List<Long> existingMembers = groupEntity.getGroupMemberIds();
        if (existingMembers == null) {
            existingMembers = new ArrayList<>();
        }

        for (Long memberId : memberMiddleware.getGroupMember()) {
            if (existingMembers.contains(memberId)) {
                throw new DataIntegrityViolationException("User with ID " + memberId + " is already a member of this team");
            }
        }

        existingMembers.addAll(memberMiddleware.getGroupMember());
        groupEntity.setGroupMemberIds(existingMembers);

        GroupEntity savedGroup = userAdapter.saveMember(groupEntity);

        return userEntityMapper.toMemberMiddleware(savedGroup);
    }

    private static final  Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public List<GroupResponse> getAllGroups(Long orgId) {
        List<Object[]> rows = userAdapter.getGroupDataNative(orgId);

        return rows.stream().map(row -> {
            GroupResponse dto = new GroupResponse();

            dto.setGroupId(((Number) row[0]).longValue());

            dto.setGroupName((String) row[1]);

            List<String> managerList = new ArrayList<>();
            Object sqlArrayObj = row[2];

            try {
                if (sqlArrayObj instanceof Array) {
                    String[] array = (String[]) ((Array) sqlArrayObj).getArray();
                    managerList = Arrays.asList(array);
                } else if (sqlArrayObj instanceof String[]) {
                    managerList = Arrays.asList((String[]) sqlArrayObj);
                } else if (sqlArrayObj instanceof List<?>) {
                    managerList = ((List<?>) sqlArrayObj).stream()
                            .map(String::valueOf)
                            .toList();
                } else if (sqlArrayObj != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    managerList = mapper.readValue(sqlArrayObj.toString(), new TypeReference<>() {});
                }
            } catch (Exception e) {
                log.error("Error parsing leadIds: {}", e.getMessage());
            }

            dto.setManagerIds(managerList);

            dto.setLocation((String) row[3]);

            try {
                String json = row[4].toString();
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> members = objectMapper.readValue(json, new TypeReference<>() {});
                dto.setGroupmember(members);
            } catch (Exception e) {
                log.error("Error parsing groupmember: {}", e.getMessage());
                dto.setGroupmember(Collections.emptyList());
            }

            return dto;
        }).toList();
    }

    @Override
    public void deleteMember(Long groupId, Long memberId) {
        userAdapter.deleteMember(groupId, memberId);
    }

    @Override
    public void deleteGroup(Long groupId) {
        userAdapter.deleteGroup(groupId);
    }

    @Override
    public List<User> getMembers(Long orgId, String role) {
        List<User> members = userAdapter.getMembers(orgId, role).stream().map(userEntityMapper::toMiddleware).toList();
        return members;
    }

    @Override
    public List<User> getMembersExcludingRole(Long orgId, String  excludedRole) {
        List<User> members = userAdapter.getMembersExcludingRole(orgId,  excludedRole).stream().map(userEntityMapper::toMiddleware).toList();
        return members;
    }

    @Override
    public List<GroupDto> getUserGroups(Long userId, Long orgId) {
        List<GroupDto> group = userAdapter.getUserGroups(userId, orgId);
        return group;
    }

    @Override
    public List<Map<String, Object>> getStudentGroupMembers(Long groupId, Long orgId) {

        GroupEntity groupEntity = userAdapter.getGroupById(groupId, orgId);
        List<Long> memberIds = groupEntity.getGroupMemberIds();

        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserEntity> studentUsers = userAdapter.getUsersByIds(memberIds, orgId);

        List<Map<String, Object>> studentDetailsList = studentUsers.stream()
//                .filter(user -> "student".equalsIgnoreCase(user.getRole().getName()))
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getUserId());
                    map.put("name", user.getUserName());
                    map.put("role", user.getRole().getName());
                    return map;
                })
                .collect(Collectors.toList());
        return studentDetailsList;
    }


}

