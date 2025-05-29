package com.uniq.tms.tms_microservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.model.AddGroup;
import com.uniq.tms.tms_microservice.model.AddMember;
import com.uniq.tms.tms_microservice.model.Group;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.model.User;
import com.uniq.tms.tms_microservice.model.UserGroup;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserService {

    List<Role> getAllRole(Long orgId, String role);
    List<Group> getAllTeam();
    List<Location> getAllLocation(Long orgId);
    ApiResponse bulkCreateUsers(MultipartFile file, Long orgId);
    ApiResponse createUser(UserDto userDto, Long organizationId);
    User updateUser(CreateUserDto updates, Long orgId, Long userId);
    List<UserResponseDto> getUsers(Long orgId, String role);
    User deleteUser(Long orgId, Long userId);
    AddGroup createGroup(AddGroup groupMiddleware, Long orgId);
    void deleteMember(Long groupId, Long memberId);
    void deleteGroup(Long groupId);
    List<User> getMembers(Long orgId, Long roleId);
    boolean updateUserGroupType(UserGroup userGroup);
    List<GroupDto>getUserGroups(Long userId, String role, Long orgId);
    List<Map<String, Object>> getGroupMembers(Long groupId, Long orgId, LocalDate date, Long userIdFromToken);
    ApiResponse addUserToGroup(AddMember addMemberMiddleware, Long orgId);
    List<GroupResponseDto> getAllGroups(Long orgId, Long userId) throws JsonProcessingException;
    UserGroup createUserGroup(UserGroup userGroupMiddleware, Long orgId);
    ApiResponse updateGroupDetails(AddGroupDto addGroupDto, Long groupId, Long orgId);
    boolean createSecondaryUser(SecondaryDetailsDto secondaryDetailsDto, UserEntity savedUser);
    List<UserNameSuggestionDto> searchUsernames(String keywords);
    UserProfileResponse getUserProfile(Long orgId, Long userId);
    List<UserNameSuggestionDto> getGroupUsers(List<Long> groupIds, Long orgId, Long loggedInUserId, String role);
    Location addLocation(LocationDto locationDto, Long orgId);
    List<Location> getUserLocation(Long userId);
    ResponseEntity<Resource> downloadSampleFile();
}
