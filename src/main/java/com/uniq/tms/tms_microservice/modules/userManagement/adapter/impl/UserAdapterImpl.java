package com.uniq.tms.tms_microservice.modules.userManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.LocationRepository;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.UserLocationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.RoleEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.*;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.GroupDto;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameEmailDto;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.*;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.*;
import com.uniq.tms.tms_microservice.modules.locationManagement.mapper.LocationEntityMapper;
import com.uniq.tms.tms_microservice.modules.userManagement.model.UserResponse;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameSuggestionDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class UserAdapterImpl implements UserAdapter {

    private static final Logger log = LogManager.getLogger(UserAdapterImpl.class);

    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final SecondaryDetailsRepository secondaryDetailsRepository;
    private final LocationEntityMapper locationEntityMapper;
    private final PrivilegeRepository privilegeRepository;
    private final UserLocationRepository userLocationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationTypeRepository organizationTypeRepository;
    private final UserSchemaMapperRepository userSchemaMapperRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserFaceRepository userFaceRepository;
    private final UserHistoryRepository userHistoryRepository;

    public UserAdapterImpl(RoleRepository roleRepository, GroupRepository groupRepository, LocationRepository locationRepository, UserRepository userRepository, UserGroupRepository userGroupRepository, SecondaryDetailsRepository secondaryDetailsRepository, LocationEntityMapper locationEntityMapper, PrivilegeRepository privilegeRepository, UserLocationRepository userLocationRepository, OrganizationRepository organizationRepository, OrganizationTypeRepository organizationTypeRepository, UserSchemaMapperRepository userSchemaMapperRepository, PlanRepository planRepository, SubscriptionRepository subscriptionRepository, UserFaceRepository userFaceRepository, UserHistoryRepository userHistoryRepository) {
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.secondaryDetailsRepository = secondaryDetailsRepository;
        this.locationEntityMapper = locationEntityMapper;
        this.privilegeRepository = privilegeRepository;
        this.userLocationRepository = userLocationRepository;
        this.organizationRepository = organizationRepository;
        this.organizationTypeRepository = organizationTypeRepository;
        this.userSchemaMapperRepository = userSchemaMapperRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userFaceRepository = userFaceRepository;
        this.userHistoryRepository = userHistoryRepository;
    }

    @Override
    public List<GroupEntity> getAllGroup(String orgId) {
        List<GroupEntity> groups = groupRepository.findAllByOrganizationEntity_OrganizationId(orgId);
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

    @Override
    public Optional<RoleEntity> findById(Long roleId) {
        return roleRepository.findById(roleId);
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
        return groupRepository.save(entity);
    }

    @Override
    public boolean findByGroup(String teamName, String orgId) {
        return groupRepository.findByGroupNameAndOrganizationId(teamName, orgId).isPresent();
    }

    @Override
    public int updateUserGroupType(String userId, Long groupId, String type) {
        return userGroupRepository.updateUserGroupType(userId,groupId,type);
    }

    @Override
    public void deleteMember(Long groupId, List<String> memberId) {
        groupRepository.deleteMemberById(groupId, memberId);
    }

    @Override
    public void deleteGroup(Long groupId, String orgId) {
        groupRepository.deleteGroupById(groupId, orgId);
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
        return groupRepository.findByUserIdAndOrganizationId(userId, orgId);
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
        return groupRepository.findByOrganizationId(orgId);
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
        return groupRepository.findByGroupId(groupId);
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
        return groupRepository.existsGroupNameInOrganization(groupName,orgId,groupId);
    }

    @Override
    public List<String> findMemberIdsByGroupId(Long groupId) {
        return userGroupRepository.findUserIdsByGroupIdAndType(groupId, "Member");
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
        List<Object[]> groups = groupRepository.findGroupNameIdMappings(orgId);
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

    @Override
    public List<UserEntity> getUsersByRoles(Set<String> roles, String orgId) {
        return userRepository.findUserByRoles(roles, orgId);
    }

    @Override
    public List<UserEntity> findUsersByRolesAndGroupIds(Set<String> roles, List<Long> supervisedGroupIds, String orgId) {
        return userRepository.findUsersByRolesAndGroupIds(roles, supervisedGroupIds, orgId);
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
    public void updateGroupWorkSchedule(String scheduleId, String scheduleId1) {
        groupRepository.updateGroupWorkSchedule(scheduleId, scheduleId1);
    }

    @Override
    public void updateUserWorkSchedule(String scheduleId, String scheduleId1) {
        userRepository.updateUserWorkSchedule(scheduleId, scheduleId1);
    }

    @Override
    public List<UserEntity> save(List<UserEntity> user) {
        return userRepository.saveAllAndFlush(user);
    }

    @Override
    public List<UserGroupEntity> findUserByOrganizationIdAndUserId(String orgId, String userId) {
        return userRepository.findUserByOrganizationIdAndUserId(orgId, userId);
    }

    @Override
    public UserSchemaMappingEntity create(UserSchemaMappingEntity entity) {
        return userSchemaMapperRepository.save(entity);
    }

    @Override
    public UserSchemaMappingEntity findUserByEmail(String email){
        return userSchemaMapperRepository.findUserByEmail(email);
    }

    @Override
    public UserSchemaMappingEntity findUserByMobile(String mobile) {
        return userSchemaMapperRepository.findUserByMobile(mobile);
    }

    @Override
    public String findByPlan() {
        return planRepository.findByIsDefault();
    }

    @Override
    public SubscriptionEntity saveSubscription(SubscriptionEntity subscriptionEntity) {
        return subscriptionRepository.save(subscriptionEntity);
    }

    @Override
    public void saveAllMappings(List<UserSchemaMappingEntity> mappings) {
        userSchemaMapperRepository.saveAll(mappings);
    }

    @Override
    public void saveAllSecondaryMappings(List<UserSchemaMappingEntity> secondaryMappings) {
        userSchemaMapperRepository.saveAll(secondaryMappings);
    }

    @Override
    public UserSchemaMappingEntity update(UserSchemaMappingEntity mapping) {
        return userSchemaMapperRepository.save(mapping);
    }

    @Override
    public Optional<UserSchemaMappingEntity> findUserByMobileAndOrgId(String mobile, String orgId) {
        return userSchemaMapperRepository.findUserByMobileAndOrgId(mobile,orgId);
    }

    @Override
    @Transactional
    public void saveUserHistory(UserHistoryEntity userHistoryEntity) {
        userHistoryRepository.save(userHistoryEntity);
    }

    @Override
    public void deleteUserFace(String userId) {
        userFaceRepository.deleteByUserId(userId);
    }

    @Override
    public List<UserHistoryEntity> getUserHistoryLog(String userId) {
        return userHistoryRepository.findByUserId(userId);
    }

    @Override
    public List<UserEntity> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    @Override
    public List<UserEntity> findUserByOrgIdAndRoleId(String orgId, int roleId) {
        return userRepository.findUserByOrganizationIdAndRole_RoleId(orgId, roleId);
    }


    @Override
    public int countTotalMembers(String orgId) {
        return (int) userRepository.countByOrganizationId(orgId);
    }

    @Override
    public int countActiveMembers(String orgId) {
        return (int) userRepository.countByOrganizationIdAndActiveTrue(orgId);
    }

    @Override
    public int countInactiveMembers(String orgId) {
        return (int) userRepository.countByOrganizationIdAndActiveFalse(orgId);
    }
    @Override
    public List<UserEntity> findByUserId(List<String> userId) {
        return userRepository.findByUserIdIn(userId);
    }
    @Override
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteByGroupIds(List<Long> groupIds) {
        userGroupRepository.deleteByGroupIds(groupIds);
    }

    @Override
    public void deleteGroups(List<Long> groupIds, String orgId) {
        userGroupRepository.deleteGroupsByIds(groupIds, orgId);
    }

    @Override
    public List<GroupEntity> findGroupsByIds(Set<Long> groupIds) {
        return groupRepository.findAllByGroupIdIn(groupIds);
    }

    @Override
    public List<UserGroupEntity> findUserGroupsByUsersAndGroups(Set<String> userIds, Set<Long> groupIds) {
        if (userIds.isEmpty() || groupIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userGroupRepository.findAllByUserIdsAndGroupIds(userIds, groupIds);
    }

    @Override
    public void deactivateUsersByIds(List<String> userIds, String orgId) {
        userRepository.deactivateUsersByIds(userIds, orgId);
    }

    @Override
    @Transactional
    public List<UserHistoryEntity> saveAllUserHistories(List<UserHistoryEntity> userHistoryEntities) {
        return userHistoryRepository.saveAll(userHistoryEntities);
    }

    @Override
    public Long getSubscribedUserLimit(String orgId) {
        return subscriptionRepository.findSubscriptionIdByOrgId(orgId);
    }

    @Override
    public Long getCurrentUserCount(String orgId) {
        return userRepository.countUsersByOrganizationId(orgId);
    }

    @Override
    public Optional<LocationEntity> findLocationById(Long locationId) {
        return locationRepository.findById(locationId);
    }

    @Override
    public boolean exists(String userId, Long locationId) {
        return userLocationRepository.existsByUser_UserIdAndLocation_LocationId(userId, locationId);
    }

    @Override
    public UserLocationEntity save(UserLocationEntity entity) {
        return userLocationRepository.save(entity);
    }



    public void bulkUpdateWorkSchedule(WorkScheduleEntity workSchedule, List<String> userIds, String orgId) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        int updatedCount = userRepository.bulkUpdateWorkSchedule(workSchedule, userIds, orgId);
    }

    @Override
    public List<UserNameEmailDto> findAdminAndSuperAdminNamesAndEmails() {
        return userRepository.findAdminAndSuperAdminNamesAndEmails();
    }

    @Override
    public List<UserEntity> findByOrganizationIdAndActiveTrue(String orgId) {
        return userRepository.findAllActiveUsersByOrganizationId(orgId);
    }

    @Override
    public int countActiveUsers(String orgId) {
        long count = userRepository.countByOrganizationIdAndActiveTrue(orgId);
        return Math.toIntExact(count);
    }

    @Override
    public void flush() {
        userRepository.flush();
    }

    @Override
    public long getUserCount(String organizationId, LocalDateTime start, LocalDateTime end) {
        return userRepository.countUsersByOrgAndCreatedAtBetween(organizationId, start, end);
    }
}
