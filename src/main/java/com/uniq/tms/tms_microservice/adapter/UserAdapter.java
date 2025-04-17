package com.uniq.tms.tms_microservice.adapter;

import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import java.util.List;
import java.util.Optional;

public interface UserAdapter {
    List<RoleEntity>getAllRole(Long orgId, String role);
    List<GroupEntity> getAllTeams();
    List<LocationEntity> getAllLocation(Long orgId);
    UserEntity saveUser(UserEntity entity);
    UserEntity updateUser(UserEntity userId);
    Optional<UserEntity> findByEmail(String email);
    void updatePassword(UserEntity user);
    boolean existsByEmail(String email);
    Optional<UserEntity> findById(Long userId);
    //List<UserResponseDto> findByOrganizationId(Long orgId, List<String> accessibleRoles);
    List<Object[]> findRawUsersWithGroups(Long orgId,List<String> accessibleRoles);
    void deactivateUserById(Long userId);
    void deleteUser(UserEntity user);
    GroupEntity saveGroup(GroupEntity entity);
    boolean findByGroup(String teamName, Long orgId);
    Optional<GroupEntity> findByTeamId(Long teamId);
    List<Object[]> getGroupDataNative(Long orgId);
    int updateUserGroupType(Long userId, Long groupId, String type);
    void deleteMember(Long groupId, Long memberId);
    void deleteGroup(Long groupId);
    List<UserEntity> getMembers(Long orgId, String  excludedRole);
    List<UserEntity> getMembersExcludingRole(Long orgId, String excludedRole);
    List<GroupDto> getUserGroups(Long userId, Long orgId);
    List<UserGroupEntity> getGroupMembersByGroupId(Long groupId, Long orgId);
    List<UserEntity> getUsersByIds(List<Long> userIds, Long orgId);
    boolean existsByMobileNumber(String mobileNumber);
    List<UserEntity> getAllUsers();
    UserEntity getUserById(Long userId);
    boolean existsGroupNameInOrganization(String groupName, Long orgId, Long groupId);
    UserGroupEntity saveUserGroup(UserGroupEntity entity);
    List<UserGroupEntity> findByUserUserIdAndGroupGroupId(Long userId, Long groupId);
    void updateSupervisorUser(Long groupId,Long newUserId);
    void updateGroupNameAndLocation(Long groupId, String groupName, Long locationId);
    void deleteSupervisorsByGroupId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);
}
