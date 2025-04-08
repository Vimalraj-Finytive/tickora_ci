package com.uniq.tms.tms_microservice.service;



import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.model.AddGroup;
import com.uniq.tms.tms_microservice.model.Group;
import com.uniq.tms.tms_microservice.model.GroupResponse;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.Member;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.model.User;


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
    Member addUserToGroup(Long groupId, Member memberMiddleware, Long orgId);
    List<GroupResponse> getAllGroups(Long orgId);
    void deleteMember(Long groupId, Long memberId);
    void deleteGroup(Long groupId);
    List<User> getMembers(Long orgId, String role);
    List<User> getMembersExcludingRole(Long orgId, String excludedRole);
    List<GroupDto>getUserGroups(Long userId, Long orgId);
    List<Map<String, Object>> getStudentGroupMembers(Long groupId, Long orgId);
}
