package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.model.UserResponse;
import com.uniq.tms.tms_microservice.repository.LocationRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.repository.TeamRepository;
import com.uniq.tms.tms_microservice.repository.UserGroupRepository;
import com.uniq.tms.tms_microservice.repository.UserRepository;
import com.uniq.tms.tms_microservice.repository.WorkScheduleRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    private final WorkScheduleRepository workScheduleRepository;

    public UserAdapterImpl(RoleRepository roleRepository, TeamRepository teamRepository, LocationRepository locationRepository, UserRepository userRepository, UserGroupRepository userGroupRepository, WorkScheduleRepository workScheduleRepository) {
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.workScheduleRepository = workScheduleRepository;
    }

    @Override
    public List<RoleEntity> getAllRole(Long orgId, int hierarchyLevel) {
        return roleRepository.findRolesByOrgIdAndRoleLevel(orgId, hierarchyLevel);
    }

    @Override
    public List<GroupEntity> getAllTeams() {
        List<GroupEntity> teams = teamRepository.findAll();
        return teams;
    }

    @Override
    public List<LocationEntity> getAllLocation(Long orgId) {
        List<LocationEntity> location = locationRepository.findAllLocationsByOrganization(orgId);
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
        return userRepository.findByUserId(userId);
    }

    public List<UserResponse> findByOrganizationId(Long orgId, int hierarchyLevel){
        return userRepository.findAllUsers(orgId,hierarchyLevel);
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
    public List<Object[]> getGroupData(Long orgId) {
        return teamRepository.getGroupData(orgId);
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
        return userRepository.findUsersByOrgIdAndRoleId(orgId, roleId);
    }

    @Override
    public List<UserEntity> getMembersByRole(Long orgId, List<Integer> higherRoleIds) {
        return userRepository.findByOrgIdAndRoleId(orgId, higherRoleIds);
    }

    @Override
    public List<GroupDto> getUserGroups(Long userId, Long orgId) {
        return teamRepository.findByUserIdAndOrganizationId(userId, orgId);
    }

    @Override
    public List<UserGroupEntity> getGroupMembersByGroupId(Long groupId, Long orgId) {
        return userGroupRepository.findUserGroups(groupId, orgId);
    }

    @Override
    public List<UserEntity> getUsersByIds(List<Long> userIds, Long orgId) {
        return userRepository.findByUserIdAndOrgId(userIds, orgId);
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
    public List<GroupDto> getAllgroups(Long orgId) {
        return teamRepository.findByOrganizationId(orgId);
    }

    @Override
    public List<UserEntity> findUsersByGroupIds(List<Long> groupIds) {
        return userGroupRepository.findUsersByGroupId(groupIds);
    }

    @Override
    public List<UserEntity> findMembersByGroupIds(
            List<Long> filteredGroupIds, Long userIdFromToken) {
        return userGroupRepository.findMembersByGroupIds(filteredGroupIds, userIdFromToken);
    }

    @Override
    public UserGroupEntity saveUserGroup(UserGroupEntity entity) {
        return userGroupRepository.save(entity);
    }

    @Override
    public List<UserGroupEntity> findByUserIdAndGroupId(Long userId, Long groupId){
        return userGroupRepository.findByUserIdAndGroupId(userId,groupId);
    };

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

    @Override
    public WorkScheduleEntity findByWorkscheduleId(Long workScheduleId) {
        return workScheduleRepository.findByScheduleId(workScheduleId);
    }

    @Override
    public WorkScheduleEntity findDefaultActiveSchedule() {
        return workScheduleRepository.findDefaultActiveSchedule();
    }
}
