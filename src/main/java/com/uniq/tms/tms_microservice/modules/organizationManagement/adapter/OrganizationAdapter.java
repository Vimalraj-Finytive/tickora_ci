package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationSummaryDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganizationAdapter {
    List<RoleEntity> getAllRole(int hierarchyLevel);
    List<RoleEntity> findAllWithPrivileges();
    Optional<RoleEntity> findRoleById(Long roleId);
    Map<String, Long> getRoleNameIdMap();
    void saveRole(RoleEntity role);
    PrivilegeEntity addPrivilege(PrivilegeEntity privilegeEntity);
    Optional<PrivilegeEntity> findPrivilegeById(Long privilegeId);
    OrganizationEntity findByOrgName(String orgName);
    List<OrganizationTypeEntity> getAllOrgType();
    Long countOrganizations();
    boolean existsByOrganizationId(String orgId);
    OrganizationEntity create(OrganizationEntity entity);
    OrganizationTypeEntity findOrgType(String orgType);
    Optional<OrganizationEntity> findByOrgId(String orgId);
    List<OrganizationSummaryDto> getOrgSummary(String orgId);
    String getOrgTypeNameById(String type);
    String getOrgName(String orgId);
    List<OrganizationEntity> findAll();
    Optional<String> findOrgTypeNameById(String orgTypeId);
    List<SubscriptionEntity> getAllSubscriptionsForOrgBetweenDates(LocalDate fromDate, LocalDate toDate);
    List<OrganizationEntity> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    List<OrganizationTypeEntity> findAllOrgType();
}
