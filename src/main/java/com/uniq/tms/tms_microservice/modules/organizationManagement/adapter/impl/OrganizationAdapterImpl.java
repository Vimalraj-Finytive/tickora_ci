package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationTypeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.RoleEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationTypeRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.PrivilegeRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.RoleRepository;
import org.springframework.stereotype.Component;
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

    public OrganizationAdapterImpl(RoleRepository roleRepository, OrganizationRepository organizationRepository, PrivilegeRepository privilegeRepository, OrganizationTypeRepository organizationTypeRepository) {
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.privilegeRepository = privilegeRepository;
        this.organizationTypeRepository = organizationTypeRepository;
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

}
