package com.uniq.tms.tms_microservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.dto.AddGroupDto;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.GroupResponseDto;
import com.uniq.tms.tms_microservice.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.model.AddGroup;
import com.uniq.tms.tms_microservice.model.AddMember;
import com.uniq.tms.tms_microservice.model.Group;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.model.User;
import com.uniq.tms.tms_microservice.model.UserGroup;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserService {

    List<Role> getAllRole(Long orgId, String role);
    List<Group> getAllTeam();
    List<Location> getAllLocation(Long orgId);
    User createUser(User usermiddleware, Long organizationId);
    User updateUser(Map<String, Object> updates, Long orgId, Long userId);
    List<UserResponseDto> getUsers(Long orgId, String role);
    User deleteUser(Long orgId, Long userId);
    AddGroup createGroup(AddGroup groupmiddleware, Long orgId);
    void deleteMember(Long groupId, Long memberId);
    void deleteGroup(Long groupId);
    List<User> getMembers(Long orgId, String role);
    List<User> getMembersExcludingRole(Long orgId, String excludedRole);
    boolean updateUserGroupType(UserGroup userGroup);
    List<GroupDto>getUserGroups(Long userId, Long orgId);
    List<Map<String, Object>> getStudentGroupMembers(Long groupId, Long orgId, LocalDate date);
    List<User> addUserToGroup(AddMember addMemberMiddleware, Long orgId);
    List<GroupResponseDto> getAllGroups(Long orgId) throws JsonProcessingException;
    UserGroup createUserGroup(UserGroup userGroupMiddleware, Long orgId);
    void updateGroupDetails(AddGroupDto addGroupDto, Long groupId, Long orgId);
}
