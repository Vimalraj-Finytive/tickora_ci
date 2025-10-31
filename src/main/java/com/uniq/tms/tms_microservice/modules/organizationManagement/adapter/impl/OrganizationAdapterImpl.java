package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationDetailsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationSummaryDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.OrganizationEntityMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OrganizationAdapterImpl implements OrganizationAdapter {

    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final PrivilegeRepository privilegeRepository;
    private final OrganizationTypeRepository organizationTypeRepository;
    private final OrganizationEntityMapper organizationEntityMapper;
    private final SubscriptionRepository subscriptionRepository;

    public OrganizationAdapterImpl(RoleRepository roleRepository, OrganizationRepository organizationRepository, PrivilegeRepository privilegeRepository, OrganizationTypeRepository organizationTypeRepository, OrganizationEntityMapper organizationEntityMapper, SubscriptionRepository subscriptionRepository) {
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.privilegeRepository = privilegeRepository;
        this.organizationTypeRepository = organizationTypeRepository;
        this.organizationEntityMapper = organizationEntityMapper;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public List<RoleEntity> getAllRole(int hierarchyLevel) {
        return roleRepository.findRolesByOrgIdAndRoleLevel(hierarchyLevel);
    }

    @Override
    public List<RoleEntity> findAllWithPrivileges() {
        return roleRepository.findAllWithPrivileges();
    }

    @Override
    public Optional<RoleEntity> findRoleById(Long roleId) {
        return roleRepository.findById(roleId);
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
    public void saveRole(RoleEntity role) {
        roleRepository.save(role);
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
    public Optional<OrganizationEntity> findByOrgId(String orgId) {
        return organizationRepository.findByOrganizationId(orgId);
    }

    @Override
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
    public OrganizationTypeEntity findOrgType(String orgType) {
        return organizationTypeRepository.findByOrgType(orgType);
    }

    @Override
    public String getOrgTypeNameById(String typeId) {
        return organizationTypeRepository.findById(typeId)
                .map(OrganizationTypeEntity::getOrgTypeName)
                .orElseThrow(() -> new RuntimeException("Organization type not found: " + typeId));
    }

    @Override
    public String getOrgName(String orgId) {
        return organizationRepository.findOrgNameByOrganizationId(orgId);
    }

    @Override
    public List<OrganizationEntity> findAll() {
        return organizationRepository.findAll();
    }

    @Override
    public List<OrganizationSummaryDto> getOrgSummary(String orgId) {
        Optional<OrganizationEntity> organizationOpt = organizationRepository.findSummaryById(orgId);
        List<OrganizationEntity> orgList = organizationOpt.map(List::of).orElse(List.of());
        return organizationEntityMapper.toMiddleware(orgList);
    }

    @Override
    public long countOrganizationsBetweenDates(LocalDateTime from, LocalDateTime to) {
        return organizationRepository.countOrganizationsBetweenDates(from, to);
    }

    @Override
    public List<Object[]> countOrganizationsByType() {
        return organizationRepository.countOrganizationsByType();
    }

    @Override
    public List<Object[]> countOrganizationsByTypeBetweenDates(LocalDateTime from, LocalDateTime to) {
        return organizationRepository.countOrganizationsByTypeBetweenDates(from, to);
    }

    @Override
    public Optional<String> findOrgTypeNameById(String orgTypeId) {
        return organizationTypeRepository.findById(orgTypeId)
                .map(OrganizationTypeEntity::getOrgTypeName);
    }

    @Override
    public List<SubscriptionEntity> getAllSubscriptionsForOrgBetweenDates(String orgId, LocalDate fromDate, LocalDate toDate) {
        return subscriptionRepository.findByOrgIdAndStartDateBetween(orgId, fromDate.atStartOfDay(), toDate.atTime(23, 59, 59));
    }

}
