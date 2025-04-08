package com.uniq.tms.tms_microservice.adapter.impl;


import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.repository.LocationRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.repository.TeamRepository;
import com.uniq.tms.tms_microservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class UserAdapterImpl implements UserAdapter {

    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public UserAdapterImpl(RoleRepository roleRepository, TeamRepository teamRepository, LocationRepository locationRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<RoleEntity> getAllRole(Long orgId, String role) {

        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Fetching roles for orgId: " + orgId + " and role: " + role);

        return roleRepository.findRolesByOrgIdAndRole(orgId, role);
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
        return userRepository.findById(userId);
    }

    @Override
    public List<UserResponseDto> findByOrganizationId(Long orgId, List<String> accessibleRoles) {
        return userRepository.findByOrganizationId(orgId, accessibleRoles);
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
    public GroupEntity saveMember(GroupEntity group) {
        return teamRepository.save(group);
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
    public List<UserEntity> getMembers(Long orgId, String  excludedRole) {
        return userRepository.findAllByOrganizationIdAndRole_Name(orgId, excludedRole);
    }

    @Override
    public List<UserEntity> getMembersExcludingRole(Long orgId, String excludedRole) {
        return userRepository.findAllByOrganizationIdAndRole_NameNot(orgId, excludedRole);
    }


    @Override
    public List<GroupDto> getUserGroups(Long userId, Long orgId) {
        return teamRepository.findByUserIdAndOrganization_id(userId, orgId);
    }

    @Override
    public GroupEntity getGroupById(Long groupId, Long orgId) {
        return teamRepository.findByGroupIdAndOrganizationEntity_OrganizationId(groupId, orgId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Override
    public List<UserEntity> getUsersByIds(List<Long> userIds, Long orgId) {
        return userRepository.findByUserIdInAndOrganizationId(userIds, orgId);
    }
}
