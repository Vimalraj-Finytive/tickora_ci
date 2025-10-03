package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationTypeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.RoleEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
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
}
