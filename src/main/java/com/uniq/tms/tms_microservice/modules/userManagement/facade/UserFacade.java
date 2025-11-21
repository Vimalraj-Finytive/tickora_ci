package com.uniq.tms.tms_microservice.modules.userManagement.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final AuthHelper authHelper;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    public UserFacade(AuthHelper authHelper, UserService userService, UserDtoMapper userDtoMapper) {

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

        userService.deleteUsers(orgId, requestDto.getUserIds(), userNameFromToken, requestDto.getComments());

        return new ApiResponse(200, "Users Inactived Successfully", null);
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

    public ApiResponse deleteMember(DeleteMemberDto request) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        DeleteMemberModel model= userDtoMapper.toModel(request);
        userService.deleteMember(model,orgId);
        return new ApiResponse(204, "Member Deleted successfully", "No Content");
    }

    public String deleteGroups(GroupBulkDeleteDto request) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid organization");
        }
        GroupBulkDeleteModel model = userDtoMapper.toModel(request);
        userService.deleteGroups(model, orgId);
        return "Groups deleted successfully";
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
        Long currentCount = userService.getCurrentUserCount(orgId);
        Long subscribedLimit = userService.getSubscribedUserLimit(orgId);
        if (!(currentCount < subscribedLimit)) {
            return new ApiResponse(404,"User creation limit reached. Please upgrade your plan.",null);
        }
        else {
            return userService.bulkCreateUsers(file, orgId, userId);
        }
    }

    public ApiResponse createUser(UserDto userDto, SecondaryDetailsDto secondaryDetailsDto) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        Long currentCount = userService.getCurrentUserCount(orgId);
        Long subscribedLimit = userService.getSubscribedUserLimit(orgId);

        if (!(currentCount < subscribedLimit)) {
            return new ApiResponse(404,"User creation limit reached. Please upgrade your plan.",null);
        }
        else {
            ApiResponse user = userService.createUser(userDto, secondaryDetailsDto, orgId);
            return new ApiResponse(HttpStatus.CREATED.value(),
                    "User created successfully",
                    user);
        }
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

    public ApiResponse<List<UserHistoryResponseDto>> getUserHistoryLog(String userId) {
        return userService.getUserHistoryLog(userId);
    }

    public ApiResponse<BulkRoleUpdateDto> updateMultipleUserRoles(BulkRoleUpdateDto request) {
        String orgId = authHelper.getOrgId();
        BulkRoleUpdateModel model = userDtoMapper.toModel(request);
        BulkRoleUpdateModel updateRole = userService.updateMultipleUserRoles(model, orgId);
        BulkRoleUpdateDto dto = userDtoMapper.toDto(updateRole);
        String message = String.format("Role Updated successfully. Uploaded Count : %d , SkippedCount : %d", dto.getUpdateCount(),dto.getSkippedCount());
        return new ApiResponse<>(200, message,null);
    }

    public ApiResponse updateWorkSchedules(BulkWorkScheduleUpdateRequestDto requestDto) {
        String orgId = authHelper.getOrgId();
        String userNameFromToken = authHelper.getUsername();

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<BulkWorkScheduleUpdateResponseDto> results =
                userService.updateWorkSchedules(requestDto, userNameFromToken, orgId);
        Map<Boolean, List<BulkWorkScheduleUpdateResponseDto>> partitioned =
                results.stream().collect(Collectors.partitioningBy(BulkWorkScheduleUpdateResponseDto::isSuccess));

        List<String> successUsers = partitioned.get(true).stream()
                .map(BulkWorkScheduleUpdateResponseDto::getMemberId)
                .toList();

        List<String> failedUsers = partitioned.get(false).stream()
                .map(BulkWorkScheduleUpdateResponseDto::getMessage)
                .toList();

        StringBuilder message = new StringBuilder();

        if (!successUsers.isEmpty()) {
            message.append("Bulk Work Schedule Update Completed. ");
            if (successUsers.size() <= 10) {
                message.append("Updated: ").append(String.join(", ", successUsers)).append(". ");
            } else {
                message.append("Updated ").append(successUsers.size()).append(" users successfully. ");
            }
        }

        if (!failedUsers.isEmpty()) {
            if (successUsers.isEmpty()) {
                message.append(String.join(", ", failedUsers)).append(".");
            } else {
                message.append("Failed: ");
                if (failedUsers.size() <= 10) {
                    message.append(String.join(", ", failedUsers)).append(".");
                } else {
                    message.append(failedUsers.size()).append(" users failed to update.");
                }
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


    public ApiResponse addOrUpdateGroupMembers(AddOrUpdateGroupMembersDto dto) {
        String orgId = authHelper.getOrgId();
        UserGroupModel model = userDtoMapper.toModel(dto);
        ApiResponse response = userService.addOrUpdateGroupMembers(orgId, model);

        return response;
    }

    public ApiResponse<BulkUserLocationDto> assignLocations(BulkUserLocationDto dto) {
        String orgId = authHelper.getOrgId();
        BulkUserLocationModel model = userDtoMapper.toModel(dto);
        BulkUserLocationModel saveUserLocation = userService.assignLocations(model,orgId);
        BulkUserLocationDto Dto=userDtoMapper.toDto(saveUserLocation);
        return new ApiResponse<>(200, "Locations assigned successfully", null);
    }

    public ApiResponse<UserCalendarRequestDto> updateCalendar(UserCalendarRequestDto updates){
        boolean success = userService.UpdateCalendar(updates);
        if (!success) {
            return new ApiResponse(401, "Unauthorized - Invalid users", null);
        }
        return new ApiResponse(200, "User Updated successfully",updates);

    }
}
