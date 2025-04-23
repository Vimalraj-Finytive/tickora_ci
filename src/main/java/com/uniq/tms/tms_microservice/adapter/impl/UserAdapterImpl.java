package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.repository.LocationRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.repository.TeamRepository;
import com.uniq.tms.tms_microservice.repository.UserGroupRepository;
import com.uniq.tms.tms_microservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class UserAdapterImpl implements UserAdapter {

    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;

    public UserAdapterImpl(RoleRepository roleRepository, TeamRepository teamRepository, LocationRepository locationRepository, UserRepository userRepository, UserGroupRepository userGroupRepository) {
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    public List<RoleEntity> getAllRole(Long orgId, int hierarchyLevel) {
        return roleRepository.findRolesByOrgIdAndRole(orgId, hierarchyLevel);
    }

    @Override
    public List<GroupEntity> getAllTeams() {
        List<GroupEntity> teams = teamRepository.findAll();
        return teams;
    }

    @Override
    public List<LocationEntity> getAllLocation(Long orgId) {
        List<LocationEntity> location = locationRepository.findByOrganizationEntity_OrganizationId(orgId);
        return location;
    }

    @Override
    @Transactional
    public UserEntity saveUser(UserEntity entity) {
        return userRepository.save(entity);
    }

    @Override
    public UserEntity updateUser(UserEntity userId) {
        return userRepository.save(userId);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Override
    public void updatePassword(UserEntity user) {
        userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<UserEntity> findById(Long userId) {
        return userRepository.findByUserIdAndActiveTrue(userId);
    }

    public List<Object[]> findRawUsersWithGroups(@Param("orgId") Long orgId, @Param("role") List<String> accessibleRoles){
        return userRepository.findRawUsersWithGroups(orgId,accessibleRoles);
    }

    @Override
    public List<UserResponseDto> findByOrganizationId(Long orgId, int heirarchyLevel) {
        return userRepository.findByOrganizationIdAndHierarchyLevel(orgId, heirarchyLevel);
    }

    @Override
    public void deactivateUserById(Long userId) {
        userRepository.deactivateUserById(userId);
    }

    @Override
    public void deleteUser(UserEntity user) {
        userRepository.delete(user);
    }

    @Override
    public GroupEntity saveGroup(GroupEntity entity) {
        return teamRepository.save(entity);
    }

    @Override
    public boolean findByGroup(String teamName, Long orgId) {
        return teamRepository.findBygroupNameAndOrganizationId(teamName, orgId).isPresent();
    }

    @Override
    public int updateUserGroupType(Long userId, Long groupId, String type) {
        return userGroupRepository.updateUserGroupType(userId,groupId,type);
    }

    @Override
    public Optional<GroupEntity> findByTeamId(Long teamId) {
        return teamRepository.findById(teamId);
    }

    @Override
    public List<Object[]> getGroupDataNative(Long orgId) {
        return teamRepository.getGroupDataNative(orgId);
    }

    @Override
    public void deleteMember(Long groupId, Long memberId) {
        teamRepository.deleteMemberById(groupId, memberId);
    }

    @Override
    public void deleteGroup(Long groupId) {
        teamRepository.deleteGroupById(groupId);
    }

    @Override
    public List<UserEntity> getMembers(Long orgId, Long roleId) {
        return userRepository.findByOrganizationIdAndRole_NameAndActiveTrue(orgId, roleId);
    }

    @Override
    public List<UserEntity> getMembersByRole(Long orgId, List<Integer> higherRoleIds) {
        return userRepository.findByOrganizationIdAndActiveTrueAndRole_NameNot(orgId, higherRoleIds);
    }

    @Override
    public List<GroupDto> getUserGroups(Long userId, Long orgId) {
        return teamRepository.findByUserIdAndOrganization_id(userId, orgId);
    }

    @Override
    public List<UserGroupEntity> getGroupMembersByGroupId(Long groupId, Long orgId) {
        return userGroupRepository.findByGroup_GroupIdAndGroup_OrganizationEntity_OrganizationId(groupId, orgId);
    }

    @Override
    public List<UserEntity> getUsersByIds(List<Long> userIds, Long orgId) {
        return userRepository.findByUserIdInAndOrganizationId(userIds, orgId);
    }

    @Override
    public boolean existsByMobileNumber(String mobileNumber) {
        return userRepository.existsByMobileNumber(mobileNumber);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
    }

    @Override
    public void deleteByGroupId( Long groupId){
        userGroupRepository.deleteByGroupId(groupId);
    }

    @Override
    public List<Long> findGroupIdsBySupervisorId(Long userIdFromToken) {
        return userGroupRepository.findGroupIdsBySupervisorId(userIdFromToken);
    }

    @Override
    public List<UserEntity> findUsersByGroupIdsExcludingSupervisors(List<Long> groupIds) {
        return userGroupRepository.findUsersByGroupIdsExcludingSupervisors(groupIds);
    }

    @Override
    public List<GroupDto> getAllgroups(Long orgId) {
        return teamRepository.findByOrganizationId(orgId);
    }

    @Override
    public List<UserEntity> findUsersByGroupIds(List<Long> groupIds) {
        return userGroupRepository.findUsersByGroupId(groupIds);
    }

    @Override
    public List<UserEntity> findUsersByGroupIdsAndRoleTypeExcludingUser(
            List<Long> filteredGroupIds, Long userIdFromToken) {
        return userGroupRepository.findUsersByGroupIdAndRoleTypeExcludingUser(filteredGroupIds, userIdFromToken);
    }

    @Override
    public UserGroupEntity saveUserGroup(UserGroupEntity entity) {
        return userGroupRepository.save(entity);
    }

    @Override
    public List<UserGroupEntity> findByUserUserIdAndGroupGroupId(Long userId, Long groupId){
        return userGroupRepository.findByUserUserIdAndGroupGroupId(userId,groupId);
    };

    @Override
    public void updateSupervisorUser(Long groupId,Long newUserId){
        userGroupRepository.updateSupervisorUser(groupId,newUserId);
    }

    @Override
    public void updateGroupNameAndLocation(Long groupId, String groupName, Long locationId){
        teamRepository.updateGroupNameAndLocation(groupId,groupName,locationId);
    }

    @Override
    public void deleteSupervisorsByGroupId( Long groupId, Long userId){
        userGroupRepository.deleteSupervisorsByGroupId(groupId, userId);
    }

    @Override
    public boolean existsGroupNameInOrganization(String groupName, Long orgId, Long groupId){
        return teamRepository.existsGroupNameInOrganization(groupName,orgId,groupId);
    }
}
