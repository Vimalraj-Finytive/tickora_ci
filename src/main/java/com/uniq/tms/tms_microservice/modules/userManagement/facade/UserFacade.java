package com.uniq.tms.tms_microservice.modules.userManagement.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.RoleMapper;
import com.uniq.tms.tms_microservice.modules.userManagement.model.*;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.*;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserFacade {
    private final RoleMapper updateRole;
    private final AuthHelper authHelper;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    public UserFacade(RoleMapper updateRole, AuthHelper authHelper, UserService userService, UserDtoMapper userDtoMapper) {
        this.updateRole = updateRole;
        this.authHelper = authHelper;
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
    }

    private final Logger log = LoggerFactory.getLogger(UserFacade.class);

    public ApiResponse getAllGroup(String orgId) {
        List<GroupDto> groups = userService.getAllGroup(orgId).stream().map(userDtoMapper::toGroupDto).toList();
        return new ApiResponse(
                200, "Groups fetched successfully", groups
        );
    }

    public ApiResponse updateUser(CreateUserDto updates, String userId) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        User user = userService.updateUser(updates, orgId, userId);
        return new ApiResponse(200, "User Updated successfully", user);
    }

    public ApiResponse getUsers() {

        String orgId = authHelper.getOrgId();
        String role = authHelper.getRole();
        role = role.replace("ROLE_", "").toUpperCase();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<UserResponseDto> users = userService.getUsers(orgId, role);
        return new ApiResponse(200, "Users fetched successfully", users);
    }

    public ApiResponse deleteUsers(com.uniq.tms.tms_microservice.dto.DeactivateUserRequestDto requestDto) {
        String orgId = authHelper.getOrgId();
        String userNameFromToken = authHelper.getUsername();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        userService.deleteUsers(orgId, requestDto.getUserIds(), userNameFromToken, requestDto);
        return new ApiResponse(200, "User Inactived successfully", "No Content");
    }

    public ApiResponse createGroup( AddGroupDto addGroupDto) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        AddGroup groupMiddleware = userDtoMapper.toMiddleware(addGroupDto);
        try {
            AddGroup createdGroup = userService.createGroup(groupMiddleware, orgId);
            return new ApiResponse(201, "Group created successfully", true);
        } catch (DataIntegrityViolationException e) {
            return new ApiResponse(409, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }

    public ApiResponse addUserToGroup( AddMemberDto addMemberDto) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        AddMember addMemberMiddleware = userDtoMapper.toMiddleware(addMemberDto);
        try {
            return userService.addUserToGroup(addMemberMiddleware, orgId);

        } catch (DataIntegrityViolationException e) {
            return new ApiResponse(409, e.getMessage(), null);
        } catch (ResponseStatusException e) {
            return new ApiResponse(e.getStatusCode().value(), e.getReason(), null);
        } catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }

    public ApiResponse updateGroupDetails( AddGroupDto addGroupDto, Long groupId) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        log.info("Updating group: groupId={}, groupName={}, locationId={}",
                groupId, addGroupDto.getGroupName(), addGroupDto.getLocationId());
        return userService.updateGroupDetails(addGroupDto, groupId, orgId);

    }

    public ApiResponse getAllGroups() throws JsonProcessingException {
        String orgId = authHelper.getOrgId();
        String userId = authHelper.getUserId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<GroupResponseDto> groups = userService.getAllGroups(orgId, userId);
        return new ApiResponse(200, "All Groups Details fetched successfully", groups);
    }

    public ApiResponse deleteMember( Long groupId, String memberId) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        userService.deleteMember(groupId, memberId,orgId);
        return new ApiResponse(204, "Member Deleted successfully", "No Content");
    }

    public ApiResponse deleteGroup( Long groupId) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        userService.deleteGroup(groupId, orgId);
        return new ApiResponse(204, "Group Deleted successfully", "No Content");
    }

    public ApiResponse getMembers( Long roleId) {

        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        List<User> members = userService.getMembers(orgId, roleId);

        // Convert to simplified structure: userId + userName
        List<Map<String, Object>> result = members.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getUserId());
                    map.put("userName", user.getUserName());
                    return map;
                })
                .toList();
        return new ApiResponse(200, "Members fetched successfully", result);
    }

    public ApiResponse updateUserGroupType( EditUserGroupDto editUserGroupDto) {

        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        UserGroup userGroup = userDtoMapper.toMiddleware(editUserGroupDto);
        boolean isUpdated = userService.updateUserGroupType(userGroup);
        if (!isUpdated) {
            throw new RuntimeException("Update failed: No matching user-group mapping found or type is the same.");
        }
        return new ApiResponse(200, "User group type updated successfully.", true);
    }

    public ApiResponse createBulkUser(MultipartFile file) {
        String orgId = authHelper.getOrgId();
        String userId = authHelper.getUserId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        return userService.bulkCreateUsers(file, orgId, userId);
    }

    public ApiResponse createUser(UserDto userDto, SecondaryDetailsDto secondaryDetailsDto) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        ApiResponse user = userService.createUser(userDto, secondaryDetailsDto,orgId);
        return new ApiResponse(HttpStatus.CREATED.value(), "User Created successfully and Reset password link sent to email.", user);
    }

    public ApiResponse getUserProfile(String userId) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        if (userId == null) {
            userId = authHelper.getUserId();
        }
        UserProfileResponseDto response = userService.getUserProfile(orgId, userId);
        return new ApiResponse(HttpStatus.OK.value(), "User Profile fetched successfully", response);
    }

    public ApiResponse getUserGroups() {
        String orgId = authHelper.getOrgId();
        String role = authHelper.getRole();
        String userId = authHelper.getUserId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<GroupDto> groups = userService.getUserGroups(userId, role, orgId).stream()
                .toList();
        return new ApiResponse(200, "User Groups fetched successfully", groups);
    }

    public ApiResponse getUserGroupMembers( Long groupId, LocalDate date) {

        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        String userIdFromToken = authHelper.getUserId();
        List<Map<String, Object>> groupMembers = userService.getGroupMembers(groupId, orgId, date, userIdFromToken);
        Map<String, Object> response = new HashMap<>();
        response.put("groupmember", groupMembers);

        return new ApiResponse(200, "Student members fetched successfully", response);
    }

    public ApiResponse searchUsernames( String keyword) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<UserNameSuggestionDto> usernames = userService.searchUsernames(keyword);
        return new ApiResponse(200, "Usernames fetched successfully", usernames);
    }

    public ApiResponse getGroupUsers( List<Long> groupIds) {
        String orgId = authHelper.getOrgId();
        String loggedInUserId = authHelper.getUserId();
        String role = authHelper.getRole();
        String userRole = role.replace("ROLE_", "");
        if (orgId == null || loggedInUserId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization or User", null);
        }
        List<UserNameSuggestionDto> users;
        users = userService.getGroupUsers(groupIds, orgId, loggedInUserId, userRole);
        return new ApiResponse(200, "Users fetched successfully", users);
    }

    public ResponseEntity<Resource> downloadSampleFile() {
        return userService.downloadSampleFile();
    }

    public ApiResponse getInactiveUsers() {
        String orgId = authHelper.getOrgId();
        String role = authHelper.getRole();
        role = role.replace("ROLE_", "").toUpperCase();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<UserResponseDto> users = userService.getInactiveUsers(orgId, role);
        return new ApiResponse(200, "Users fetched successfully", users);
    }

    public ApiResponse updateIsActive(EditUserDto editUserDto) {
        String orgId = authHelper.getOrgId();
        String userNameFromToken = authHelper.getUsername();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<EditUserDto> editUserDtos = userService.updateIsActive(userDtoMapper.toMiddleware(editUserDto), orgId, userNameFromToken);
        return new ApiResponse(200,"User is activated", null);
    }

    public ApiResponse<UserValidationDto> validateUser(String userId) {
        String orgSchema = authHelper.getSchema();
        return userService.validateUser(userId);
    }

    public ApiResponse<List<UserHistoryResponseDto>> getUserHistoryLog(String userId) {
        return userService.getUserHistoryLog(userId);
    }
    public Iterable<BulkRoleUpdate> updateMultipleUserRoles(List<String> userIds, Long roleId) {
        String orgId = authHelper.getOrgId();
        List<UserBulkChangingModel> user= userService.updateMultipleUserRoles(userIds, roleId, orgId);
        Iterable<BulkRoleUpdate> result=user.stream()
                .map(updateRole::toDto).collect(Collectors.toList());
        return result;
    }
    public ApiResponse updateWorkSchedules(BulkWorkScheduleUpdateRequestDto requestDto) {
        String orgId = authHelper.getOrgId();
        String userNameFromToken = authHelper.getUsername();

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<BulkWorkScheduleUpdateResponseDto> results =
                userService.updateWorkSchedules(requestDto,userNameFromToken,orgId); // orgID, dto, username

        List<String> successUsers = results.stream()
                .filter(BulkWorkScheduleUpdateResponseDto::isSuccess)
                .map(BulkWorkScheduleUpdateResponseDto::getMemberId)
                .collect(Collectors.toList());

        List<String> failedUsers = results.stream()
                .filter(r -> !r.isSuccess())
                .map(BulkWorkScheduleUpdateResponseDto::getMessage) // <-- fixed here
                .collect(Collectors.toList());

        StringBuilder message = new StringBuilder();

        if (!successUsers.isEmpty()) {
            message.append("Bulk Work Schedule Update Completed. ");
            message.append("Updated: ").append(String.join(", ", successUsers)).append(". ");
        }

        if (!failedUsers.isEmpty()) {
            if (successUsers.isEmpty()) {
                message.append(String.join(", ", failedUsers)).append(".");
            } else {
                message.append("Failed: ").append(String.join(", ", failedUsers)).append(".");
            }
        }

        if (successUsers.isEmpty() && failedUsers.isEmpty()) {
            message.append("No updates were made.");
        }

        int statusCode = 200;
        if (failedUsers.size() == 1 && failedUsers.get(0).startsWith("Work Schedule not found")) {
            statusCode = 404;
        }

        return new ApiResponse(statusCode, message.toString().trim(), null);
    }


}
