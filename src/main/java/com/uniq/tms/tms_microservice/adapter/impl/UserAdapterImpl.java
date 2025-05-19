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
import com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class UserAdapterImpl implements UserAdapter {

    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final SecondaryDetailsRepository secondaryDetailsRepository;

    public UserAdapterImpl(RoleRepository roleRepository, TeamRepository teamRepository, LocationRepository locationRepository, UserRepository userRepository, UserGroupRepository userGroupRepository, WorkScheduleRepository workScheduleRepository, SecondaryDetailsRepository secondaryDetailsRepository) {
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.secondaryDetailsRepository = secondaryDetailsRepository;
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
        return userGroupRepository.findActiveUserGroups(groupId, orgId);
    }

    @Override
    public List<UserEntity> getUsersByIds(List<Long> userIds, Long orgId) {
        return userRepository.findByUserIdAndOrgIdAndActiveTrue(userIds, orgId);
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

    public SecondaryDetailsEntity saveSecondaryDetails(SecondaryDetailsEntity secondaryDetails) {
        return secondaryDetailsRepository.save(secondaryDetails);
    }

    @Override
    public boolean existsMobileByMobile(String mobile) {
        return secondaryDetailsRepository.existsMobileByMobile(mobile);
    }

    @Override
    public boolean existsEmailByEmail(String email) {
        return secondaryDetailsRepository.existsEmailByEmail(email);
    }

    @Override
    public List<UserNameSuggestionDto> searchUserNamesContaining(String keyword) {
        return userRepository.searchUserNamesContaining(keyword);
    }

    @Override
    public Optional<RoleEntity> findRoleById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    @Override
    public Optional<SecondaryDetailsEntity> findSecondaryUserById(Long userId) {
        return secondaryDetailsRepository.findByUserId(userId);
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public List<Long> findSupervisorIdsByGroupId(Long groupId) {
        return userGroupRepository.findSupervisorIdsByGroupId(groupId);
    }

    @Override
    public Optional<GroupEntity> findByGroupId(Long groupId) {
        return teamRepository.findByGroupId(groupId);
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
    public List<Long> findMemberIdsByGroupId(Long groupId) {
        return userGroupRepository.findUserIdsByGroupIdAndType(groupId, "Member");
    }

    @Override
    public LocationEntity findLocationById(Long locationId) {
        return locationRepository.findById(locationId).orElse(null);
    }

    @Override
    public List<UserGroupEntity> findUserByOrganizationIdAndUserId(Long organizationId, Long userId) {
        return userRepository.findUserByOrganizationIdAndUserId(organizationId, userId);
    }

    public List<UserEntity> filterUsersByGroupIds(Long supervisorId, List<UserEntity> targetUsers) {
        // Extract userIds from the targetUsers list
        List<Long> userIds = targetUsers.stream()
                .map(UserEntity::getUserId)
                .toList();
        // Query the repository to filter users based on their groups and supervisorId
        return userGroupRepository.filterUsersByGroupIds(supervisorId, userIds);
    }

    @Override
    public List<RoleEntity> findAllWithPrivileges() {
        return roleRepository.findAllWithPrivileges();
    }

    @Override
    public List<UserGroupEntity> getGroupUsersByGroupId(List<Long> groupIds, Long orgId) {
        return userGroupRepository.findActiveGroupMembersExcludingSupervisors(groupIds, orgId);
    }

    @Override
    public List<UserNameSuggestionDto> getAllActiveUsers(Long orgId, int hierarchyLevel) {
        return userRepository.findAllActiveUsersByOrganization(orgId, hierarchyLevel);
    }

    @Override
    public List<UserNameSuggestionDto> getAllGroupUsers(List<Long> groupIds, Long orgId) {
        return userRepository.findAllGroupUsersByOrganizationId(groupIds,orgId);
    }


    @Override
    public Long getRoleIdByName(String roleName) {
        RoleEntity role = roleRepository.findByNameIgnoreCase(roleName);
        return role != null ? role.getRoleId() : null;
    }
    @Override
    public Long getLocationIdByName(String locationName) {
        LocationEntity location = locationRepository.findByNameIgnoreCase(locationName);
        return location != null ? location.getLocationId() : null;
    }

    @Override
    public Set<String> getAllMobileNumbers() {
        List<String> mobiles = userRepository.findAllMobileNumbers();
        return new HashSet<>(mobiles);
    }

    @Override
    public Set<String> getAllEmails() {
        List<String> emails = userRepository.findAllEmails();
        return new HashSet<>(emails);
    }

    @Override
    public Map<String, Long> getRoleNameIdMap() {
        List<Object[]> roles = roleRepository.findRoleNameIdMappings();
        Map<String, Long> roleNameToIdMap = new HashMap<>();
        for (Object[] role : roles) {
            roleNameToIdMap.put(((String) role[0]).toLowerCase(), (Long) role[1]);
        }
        return roleNameToIdMap;
    }

    @Override
    public Map<String, Long> getLocationNameToIdMap() {
        List<Object[]> locations = locationRepository.findLocationNameIdMappings();
        Map<String, Long> locationNameToIdMap = new HashMap<>();
        for (Object[] location : locations) {
            locationNameToIdMap.put(((String) location[0]).toLowerCase(), (Long) location[1]);
        }
        return locationNameToIdMap;
    }

    @Override
    public List<UserEntity> saveAllUsers(List<UserEntity> users) {
        return userRepository.saveAll(users);
    }

    @Override
    public List<SecondaryDetailsEntity> saveAllSecondaryDetails(List<SecondaryDetailsEntity> details) {
        return secondaryDetailsRepository.saveAll(details);
    }
    @Override
    public Map<String, Long> getGroupNameIdMap() {
        List<Object[]> groups = teamRepository.findGroupNameIdMappings();
        Map<String, Long> groupNameToIdMap = new HashMap<>();
        for (Object[] group : groups) {
            groupNameToIdMap.put(((String) group[0]).toLowerCase(), (Long) group[1]);
        }
        return groupNameToIdMap;
    }

    @Override
    public List<UserGroupEntity> saveAllUserGroups(List<UserGroupEntity> userGroups){
        return userGroupRepository.saveAll(userGroups);
    }

    @Override
    public RoleEntity getRoleWithPrivileges(Long roleId){
        return roleRepository.findByIdWithPrivileges(roleId);
    }

    @Override
    public Set<String> getAllSecondaryEmail(){
        return  new HashSet<>(secondaryDetailsRepository.findAllEmail());
    }

    @Override
    public Set<String> getAllSecondaryMobile(){
        return  new HashSet<>(secondaryDetailsRepository.findAllMobile());
    }

    @Override
    public UserEntity findUserByOrgIdAndUserId(Long orgId, Long userId) {
        return userRepository.findByOrganizationIdAndUserId(orgId, userId);
    }
}
