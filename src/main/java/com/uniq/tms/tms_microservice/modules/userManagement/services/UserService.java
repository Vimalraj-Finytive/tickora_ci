package com.uniq.tms.tms_microservice.modules.userManagement.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Organization;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.userManagement.model.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserService {

    List<Group> getAllGroup(String orgId);

    ApiResponse bulkCreateUsers(MultipartFile file, String orgId, String userId);

    ApiResponse<UserDto> createUser(UserDto userDto, SecondaryDetailsDto secondaryDetailsDto, String organizationId);

    User updateUser(CreateUserDto updates, String orgId, String userId);

    List<UserResponseDto> getUsers(String orgId, String role);

    void deleteUsers(String orgId, List<String> userIds, String userNameFromToken, String comments);

    AddGroup createGroup(AddGroup groupMiddleware, String orgId);

    void deleteMember(DeleteMemberModel model, String orgId);

    void deleteGroups(GroupBulkDeleteModel model, String orgId);

    List<User> getMembers(String orgId, Long roleId);

    boolean updateUserGroupType(UserGroup userGroup);
    List<GroupDto> getUserGroups(String userId, String role, String orgId);
    List<Map<String, Object>> getGroupMembers(Long groupId, String orgId, LocalDate date, String userIdFromToken);

    ApiResponse addUserToGroup(AddMember addMemberMiddleware, String orgId);

    List<GroupResponseDto> getAllGroups(String orgId, String userId) throws JsonProcessingException;

    UserGroup createUserGroup(UserGroup userGroupMiddleware, String orgId);

    ApiResponse updateGroupDetails(AddGroupDto addGroupDto, Long groupId, String orgId);

    List<UserNameSuggestionDto> searchUsernames(String keywords);

    UserProfileResponseDto getUserProfile(String orgId, String userId);

    List<UserNameSuggestionDto> getGroupUsers(List<Long> groupIds, String orgId, String loggedInUserId, String role);

    ResponseEntity<Resource> downloadSampleFile();

    String findGroupName(Long requestedGroupId);

    List<UserResponseDto> getInactiveUsers(String orgId, String role);

    List<EditUserDto> updateIsActive(EditUser editUser, String orgId, String userNameFromToken);

    ApiResponse createSuperAdminUser(Organization organization, String orgId, String schemaName);

    ApiResponse<List<UserHistoryResponseDto>> getUserHistoryLog(String userId);

    BulkRoleUpdateModel updateMultipleUserRoles(BulkRoleUpdateModel model, String orgId);

    List<BulkWorkScheduleUpdateResponseDto> updateWorkSchedules(BulkWorkScheduleUpdateRequestDto requestDto, String userNameFromToken, String orgId);

    ApiResponse addOrUpdateGroupMembers(String orgId, UserGroupModel model);

    Long getSubscribedUserLimit(String orgId);

    Long getCurrentUserCount(String orgId);

    BulkUserLocationModel assignLocations(BulkUserLocationModel model, String orgId);

    boolean UpdateCalendar(UserCalendarRequestDto updates);

    List<UserLevelModel> getUsersBelowHierarchy(String userId, String orgId);

    List<GroupModel> getSupervisorGroups(String userId);

    List<UserLevelModel> getGroupMembers(Long groupId);

    List<UserLevelModel> getRequesters();

    RequestApproverModel assignRequestApprover(RequestApproverDto dto);
}
