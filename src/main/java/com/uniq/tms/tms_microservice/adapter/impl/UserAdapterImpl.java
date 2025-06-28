package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.mapper.LocationEntityMapper;
import com.uniq.tms.tms_microservice.model.Location;
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
    private final SecondaryDetailsRepository secondaryDetailsRepository;
    private final LocationEntityMapper locationEntityMapper;
    private final PrivilegeRepository privilegeRepository;
    private final UserLocationRepository userLocationRepository;
    private final OrganizationRepository organizationRepository;

    public UserAdapterImpl(RoleRepository roleRepository, TeamRepository teamRepository, LocationRepository locationRepository, UserRepository userRepository, UserGroupRepository userGroupRepository, SecondaryDetailsRepository secondaryDetailsRepository, LocationEntityMapper locationEntityMapper, PrivilegeRepository privilegeRepository, UserLocationRepository userLocationRepository, OrganizationRepository organizationRepository) {
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.secondaryDetailsRepository = secondaryDetailsRepository;
        this.locationEntityMapper = locationEntityMapper;
        this.privilegeRepository = privilegeRepository;
        this.userLocationRepository = userLocationRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public List<RoleEntity> getAllRole(Long orgId, int hierarchyLevel) {
        return roleRepository.findRolesByOrgIdAndRoleLevel(orgId, hierarchyLevel);
    }

    @Override
    public List<GroupEntity> getAllGroup(Long orgId) {
        List<GroupEntity> groups = teamRepository.findAllByOrganizationEntity_OrganizationId(orgId);
        return groups;
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
    public Optional<UserEntity> findById(Long userId) {
        return userRepository.findByUserId(userId);
    }

    public List<UserResponse> findByOrganizationId(Long orgId, int hierarchyLevel){
        return userRepository.findAllUsers(orgId,hierarchyLevel);
    }

    @Override
    public void deactivateUserById(Long userId, Long orgId) {
        userRepository.deactivateUserById(userId, orgId);
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
    public List<Object[]> getGroupData(Long orgId) {
        return teamRepository.getGroupData(orgId);
    }

    @Override
    public void deleteMember(Long groupId, Long memberId) {
        teamRepository.deleteMemberById(groupId, memberId);
    }

    @Override
    public void deleteGroup(Long groupId, Long orgId) {
        teamRepository.deleteGroupById(groupId, orgId);
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
    public Optional<UserEntity> findByMobileNumber(String mobileNumber) {
        return userRepository.findOptionalByMobileNumber(mobileNumber);
    }

    @Override
    public List<UserEntity> getAllUsers(Long orgId, Long userIdFromToken, int hierarchyLevel) {
        return userRepository.findAllUsersList(orgId, userIdFromToken, hierarchyLevel);
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
    public Optional<SecondaryDetailsEntity> findByMobileByMobile(String mobile) {
        return secondaryDetailsRepository.findByMobile(mobile);
    }

    @Override
    public Optional<SecondaryDetailsEntity> findByEmailByEmail(String email) {
        return secondaryDetailsRepository.findByEmail(email);
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
    public LocationEntity addLocation(Location location) {
        LocationEntity locationEntity = locationEntityMapper.toEntity(location);
        return locationRepository.save(locationEntity);
    }

    @Override
    public PrivilegeEntity addPrivilege(PrivilegeEntity privilegeEntity) {
        return privilegeRepository.save(privilegeEntity);
    }

    @Override
    public Optional<PrivilegeEntity> findPrivilegeById(Long privilegeId) {
        return privilegeRepository.findById(privilegeId);
    }

    @Override
    public void saveRole(RoleEntity role) {
        roleRepository.save(role);
    }

    @Override
    public List<UserEntity> getUsersByRoles(Set<String> roles, Long orgId) {
        return userRepository.findUserByRoles(roles, orgId);
    }

    @Override
    public List<UserEntity> findUsersByRolesAndGroupIds(Set<String> roles, List<Long> supervisedGroupIds, Long orgId) {
        return userRepository.findUsersByRolesAndGroupIds(roles, supervisedGroupIds, orgId);
    }

    @Override
    public void saveUserLocation(List<UserLocationEntity> entityList) {
        userLocationRepository.saveAll(entityList);
    }

    @Override
    public List<UserLocationEntity> findUserLocationByUserId(Long userId) {
        return userLocationRepository.findByUser_UserId(userId);
    }

    @Override
    public List<LocationEntity> findAllLocationById(List<Long> locationIds) {
        return locationRepository.findAllById(locationIds);
    }

    @Override
    @Transactional
    public void deleteUserLocationByUserId(Long userId, Set<Long> toDelete) {
        userLocationRepository.deleteByUser_UserIdAndLocation_LocationIdIn(userId, toDelete);
    }

    @Override
    @Transactional
    public void updateUserLocationByUserId(List<UserLocationEntity> newEntities) {
        userLocationRepository.saveAll(newEntities);
    }

    @Override
    public List<UserGroupEntity> findUserGroupByUserId(Long userId){
        return userGroupRepository.findGroupByUser_UserId(userId);
    }

    @Override
    @Transactional
    public void deleteUserGroupByUserId(Long userId, Set<Long> toDelete) {
        userGroupRepository.deleteByUser_UserIdAndGroup_GroupIdIn(userId, toDelete);
    }

    @Override
    public void updateUserGroupByUserId(List<UserGroupEntity> newEntities) {
        userGroupRepository.saveAll(newEntities);
    }

    @Override
    public OrganizationEntity findByOrgId(Long orgId) {
        return organizationRepository.findById(orgId).orElse(null);
    }

    @Override
    public boolean findByLocation(String name, Long orgId) {
        return locationRepository.findByNameAndOrganizationId(name, orgId).isPresent();
    }

    @Override
    public List<UserEntity> findByRoleId(List<Long> roleIds, Long orgId){
        return roleRepository.findByIdIn(roleIds, orgId);
    }

}
