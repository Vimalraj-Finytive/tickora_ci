package com.uniq.tms.tms_microservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.model.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserService {

    List<Role> getAllRole(String orgId, String role);
    List<Group> getAllGroup(String orgId);
    List<Location> getAllLocation(String orgId);
    ApiResponse bulkCreateUsers(MultipartFile file, String orgId, Long userId);
    ApiResponse createUser(UserDto userDto,SecondaryDetailsDto secondaryDetailsDto, String organizationId);
    User updateUser(CreateUserDto updates, String orgId, Long userId);
    List<UserResponseDto> getUsers(String orgId, String role);
    User deleteUser(String orgId, Long userId);
    AddGroup createGroup(AddGroup groupMiddleware, String orgId);
    void deleteMember(Long groupId, Long memberId, String orgId);
    void deleteGroup(Long groupId, String orgId);
    List<User> getMembers(String orgId, Long roleId);
    boolean updateUserGroupType(UserGroup userGroup);
    List<GroupDto>getUserGroups(Long userId, String role, String orgId);
    List<Map<String, Object>> getGroupMembers(Long groupId, String orgId, LocalDate date, Long userIdFromToken);
    ApiResponse addUserToGroup(AddMember addMemberMiddleware, String orgId);
    List<GroupResponseDto> getAllGroups(String orgId, Long userId) throws JsonProcessingException;
    UserGroup createUserGroup(UserGroup userGroupMiddleware, String orgId);
    ApiResponse updateGroupDetails(AddGroupDto addGroupDto, Long groupId, String orgId);
    List<UserNameSuggestionDto> searchUsernames(String keywords);
    UserProfileResponse getUserProfile(String orgId, Long userId);
    List<UserNameSuggestionDto> getGroupUsers(List<Long> groupIds, String orgId, Long loggedInUserId, String role);
    Location addLocation(LocationDto locationDto, String orgId);
    List<LocationDto> getUserLocation(Long userId);
    ResponseEntity<Resource> downloadSampleFile();
    Privilege addPrivileges(Privilege privilegeModel, String orgId);
    RolePrivilege addRolwisePrivileges(RolePrivilege rolePrivilegeModel, String orgId);
    ApiResponse updateLocation(String orgId, LocationList location);
    void deleteLocation(LocationListDto locationIds, String orgId);
}
