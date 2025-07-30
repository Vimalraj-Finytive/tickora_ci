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
    private final OrganizationTypeRepository organizationTypeRepository;

    public UserAdapterImpl(RoleRepository roleRepository, TeamRepository teamRepository, LocationRepository locationRepository, UserRepository userRepository, UserGroupRepository userGroupRepository, SecondaryDetailsRepository secondaryDetailsRepository, LocationEntityMapper locationEntityMapper, PrivilegeRepository privilegeRepository, UserLocationRepository userLocationRepository, OrganizationRepository organizationRepository, OrganizationTypeRepository organizationTypeRepository) {
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
        this.organizationTypeRepository = organizationTypeRepository;
    }

    @Override
    public List<RoleEntity> getAllRole(int hierarchyLevel) {
        return roleRepository.findRolesByOrgIdAndRoleLevel(hierarchyLevel);
    }

    @Override
    public List<GroupEntity> getAllGroup(String orgId) {
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
    public Optional<UserEntity> findById(String userId) {
        return userRepository.findByUserId(userId);
    }

    public List<UserResponse> findByOrganizationId(String orgId, int hierarchyLevel){
        return userRepository.findAllUsers(orgId,hierarchyLevel);
    }

    @Override
    public void deactivateUserById(String userId, String orgId) {
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
    public boolean findByGroup(String teamName, String orgId) {
        return teamRepository.findBygroupNameAndOrganizationId(teamName, orgId).isPresent();
    }

    @Override
    public int updateUserGroupType(String userId, Long groupId, String type) {
        return userGroupRepository.updateUserGroupType(userId,groupId,type);
    }

    @Override
    public void deleteMember(Long groupId, String memberId) {
        teamRepository.deleteMemberById(groupId, memberId);
    }

    @Override
    public void deleteGroup(Long groupId, String orgId) {
        teamRepository.deleteGroupById(groupId, orgId);
    }

    @Override
    public List<UserEntity> getMembers(String orgId, Long roleId) {
        return userRepository.findUsersByOrgIdAndRoleId(orgId, roleId);
    }

    @Override
    public List<UserEntity> getMembersByRole(String orgId, List<Integer> higherRoleIds) {
        return userRepository.findByOrgIdAndRoleId(orgId, higherRoleIds);
    }

    @Override
    public List<GroupDto> getUserGroups(String userId, String orgId) {
        return teamRepository.findByUserIdAndOrganizationId(userId, orgId);
    }

    @Override
    public List<UserGroupEntity> getGroupMembersByGroupId(Long groupId, String orgId) {
        return userGroupRepository.findActiveUserGroups(groupId, orgId);
    }

    @Override
    public List<UserEntity> getUsersByIds(List<String> userIds, String orgId) {
        return userRepository.findByUserIdAndOrgIdAndActiveTrue(userIds, orgId);
    }

    @Override
    public Optional<UserEntity> findByMobileNumber(String mobileNumber) {
        return userRepository.findOptionalByMobileNumber(mobileNumber);
    }

    @Override
    public List<UserEntity> getAllUsers(String orgId, String userIdFromToken, int hierarchyLevel) {
        return userRepository.findAllUsersList(orgId, userIdFromToken, hierarchyLevel);
    }


    @Override
    public UserEntity getUserById(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
    }

    @Override
    public void deleteByGroupId( Long groupId){
        userGroupRepository.deleteByGroupId(groupId);
    }

    @Override
    public List<Long> findGroupIdsBySupervisorId(String userIdFromToken) {
        return userGroupRepository.findGroupIdsBySupervisorId(userIdFromToken);
    }

    @Override
    public List<GroupDto> getAllgroups(String orgId) {
        return teamRepository.findByOrganizationId(orgId);
    }

    @Override
    public List<UserEntity> findUsersByGroupIds(List<Long> groupIds) {
        return userGroupRepository.findUsersByGroupId(groupIds);
    }

    @Override
    public List<UserEntity> findMembersByGroupIds(
            List<Long> filteredGroupIds, String userIdFromToken) {
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
    public Optional<SecondaryDetailsEntity> findSecondaryUserById(String userId) {
        return secondaryDetailsRepository.findByUserId(userId);
    }

    @Override
    public boolean existsById(String userId) {
        return userRepository.existsByUserId(userId);
    }

    @Override
    public List<String> findSupervisorIdsByGroupId(Long groupId) {
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
    public List<UserGroupEntity> findByUserIdAndGroupId(String userId, Long groupId){
        return userGroupRepository.findByUserIdAndGroupId(userId,groupId);
    };

    @Override
    public void deleteSupervisorsByGroupId( Long groupId, String userId){
        userGroupRepository.deleteSupervisorsByGroupId(groupId, userId);
    }

    @Override
    public boolean existsGroupNameInOrganization(String groupName, String orgId, Long groupId){
        return teamRepository.existsGroupNameInOrganization(groupName,orgId,groupId);
    }

    @Override
    public List<String> findMemberIdsByGroupId(Long groupId) {
        return userGroupRepository.findUserIdsByGroupIdAndType(groupId, "Member");
    }

    @Override
    public LocationEntity findLocationById(Long locationId, String orgId) {
        return locationRepository.findByLocationIdAndOrganizationEntity_OrganizationId(locationId, orgId);
    }

    @Override
    public List<RoleEntity> findAllWithPrivileges() {
        return roleRepository.findAllWithPrivileges();
    }

    @Override
    public List<UserGroupEntity> getGroupUsersByGroupId(List<Long> groupIds, String orgId) {
        return userGroupRepository.findActiveGroupMembersExcludingSupervisors(groupIds, orgId);
    }

    @Override
    public List<UserNameSuggestionDto> getAllActiveUsers(String orgId, int hierarchyLevel) {
        return userRepository.findAllActiveUsersByOrganization(orgId, hierarchyLevel);
    }

    @Override
    public List<UserNameSuggestionDto> getAllGroupUsers(List<Long> groupIds, String orgId) {
        return userRepository.findAllGroupUsersByOrganizationId(groupIds,orgId);
    }

    @Override
    public Set<String> getAllMobileNumbers(String orgId) {
        List<String> mobiles = userRepository.findAllMobileNumbers(orgId);
        return new HashSet<>(mobiles);
    }

    @Override
    public Set<String> getAllEmails(String orgId) {
        List<String> emails = userRepository.findAllEmails(orgId);
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
    public Map<String, Long> getLocationNameToIdMap(String orgId) {
        List<Object[]> locations = locationRepository.findLocationNameIdMappings(orgId);
        Map<String, Long> locationNameToIdMap = new HashMap<>();
        for (Object[] location : locations) {
            locationNameToIdMap.put(((String) location[0]).toLowerCase(), (Long) location[1]);
        }
        return locationNameToIdMap;
    }

    @Override
    @Transactional
    public List<UserEntity> saveAllUsers(List<UserEntity> users) {
        return userRepository.saveAll(users);
    }

    @Override
    public List<SecondaryDetailsEntity> saveAllSecondaryDetails(List<SecondaryDetailsEntity> details) {
        return secondaryDetailsRepository.saveAll(details);
    }
    @Override
    public Map<String, Long> getGroupNameIdMap(String orgId) {
        List<Object[]> groups = teamRepository.findGroupNameIdMappings(orgId);
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
    public Set<String> getAllSecondaryEmail(String orgId){
        return  new HashSet<>(secondaryDetailsRepository.findAllEmail(orgId));
    }

    @Override
    public Set<String> getAllSecondaryMobile(String orgId){
        return  new HashSet<>(secondaryDetailsRepository.findAllMobile(orgId));
    }

    @Override
    public UserEntity findUserByOrgIdAndUserId(String orgId, String userId) {
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
    public List<UserEntity> getUsersByRoles(Set<String> roles, String orgId) {
        return userRepository.findUserByRoles(roles, orgId);
    }

    @Override
    public List<UserEntity> findUsersByRolesAndGroupIds(Set<String> roles, List<Long> supervisedGroupIds, String orgId) {
        return userRepository.findUsersByRolesAndGroupIds(roles, supervisedGroupIds, orgId);
    }

    @Override
    public void saveUserLocation(List<UserLocationEntity> entityList) {
        userLocationRepository.saveAll(entityList);
    }

    @Override
    public List<UserLocationEntity> findUserLocationByUserId(String userId) {
        return userLocationRepository.findByUser_UserId(userId);
    }

    @Override
    public List<LocationEntity> findAllLocationById(List<Long> locationIds) {
        return locationRepository.findAllById(locationIds);
    }

    @Override
    @Transactional
    public void deleteUserLocationByUserId(String userId, Set<Long> toDelete) {
        userLocationRepository.deleteByUser_UserIdAndLocation_LocationIdIn(userId, toDelete);
    }

    @Override
    @Transactional
    public void updateUserLocationByUserId(List<UserLocationEntity> newEntities) {
        userLocationRepository.saveAll(newEntities);
    }

    @Override
    public List<UserGroupEntity> findUserGroupByUserId(String userId){
        return userGroupRepository.findGroupByUser_UserId(userId);
    }

    @Override
    @Transactional
    public void deleteUserGroupByUserId(String userId, Set<Long> toDelete) {
        userGroupRepository.deleteByUser_UserIdAndGroup_GroupIdIn(userId, toDelete);
    }

    @Override
    public void updateUserGroupByUserId(List<UserGroupEntity> newEntities) {
        userGroupRepository.saveAll(newEntities);
    }

    @Override
    public boolean findByLocation(String name, String orgId) {
        return locationRepository.findByNameAndOrganizationId(name, orgId).isPresent();
    }

    @Override
    public List<UserEntity> findByRoleId(List<Long> roleIds, String orgId){
        return roleRepository.findByIdIn(roleIds, orgId);
    }

    @Override
    public List<LocationEntity> updateMultipleLocations(List<LocationEntity> updatedEntities) {
        return locationRepository.saveAll(updatedEntities);
    }

    @Override
    public void deleteLocation(List<Long> locationIds, String orgId) {
        locationRepository.deleteAllLocationById(locationIds, orgId);
    }

    @Override
    public Optional<LocationEntity> findAllDefaultLocationById(List<Long> locationIds, String orgId) {
        return locationRepository.findDefaultLocationByOrgId(locationIds, orgId);
    }

    @Override
    public LocationEntity findDefaultLocationByOrgId(String orgId) {
        return locationRepository.findDefaultLocationById(orgId);
    }

    @Override
    public List<GroupEntity> findByLocation_LocationIdIn(List<Long> defaultLocationId) {
        return teamRepository.findByLocationEntity_LocationIdIn(defaultLocationId);
    }

    @Override
    public void saveAllGroups(List<GroupEntity> groupsToUpdate) {
        teamRepository.saveAll(groupsToUpdate);
    }

    @Override
    public List<UserLocationEntity> findUserLocationByLocationId(List<Long> defaultLocationId) {
        return  userLocationRepository.findByLocation_LocationIdIn(defaultLocationId);
    }

    @Override
    public Optional<OrganizationEntity> findByOrgId(String orgId) {
        return organizationRepository.findByOrganizationId(orgId);
    }

    @Override
    public void updateUserWorkSchedule(String scheduleId, String scheduleId1) {
        userRepository.updateUserWorkSchedule(scheduleId, scheduleId1);
    }

    @Override
    public void updateGroupWorkSchedule(String scheduleId, String scheduleId1) {
        teamRepository.updateGroupWorkSchedule(scheduleId, scheduleId1);
    }

    public OrganizationEntity create(OrganizationEntity entity){
        return organizationRepository.save(entity);
    }

    @Override
    public OrganizationEntity findByOrgName(String organization) {
        return organizationRepository.findByOrgName(organization);
    }

    @Override
    public List<OrganizationTypeEntity> getAllOrgType() {
        return organizationTypeRepository.findAll();
    }

    @Override
    public Long countOrganizations() {
        return organizationRepository.findNextOrganizationId();
    }

    @Override
    public boolean existsByOrganizationId(String orgId) {
        return organizationRepository.existsById(orgId);
    }

    @Override
    public List<LocationEntity> findLocation(String orgId) {
        return locationRepository.findLocationByOrganizationEntity_OrganizationId(orgId);
    }

    @Override
    public UserEntity findUserByOrgIdAndRoleId(String orgId, int roleId) {
        return userRepository.findUserByOrganizationIdAndRole_RoleId(orgId, roleId);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    public OrganizationTypeEntity findOrgType(String orgType) {
        return organizationTypeRepository.findByorgType(orgType);
    }

    @Override
    public void saveAllUserLocation(List<UserLocationEntity> newUserLocationsToInsert) {
        userLocationRepository.saveAll(newUserLocationsToInsert);
    }

    @Override
    public void deleteAllUserLocations(List<UserLocationEntity> userLocationsToDelete) {
        userLocationRepository.deleteAll(userLocationsToDelete);
    }
}
