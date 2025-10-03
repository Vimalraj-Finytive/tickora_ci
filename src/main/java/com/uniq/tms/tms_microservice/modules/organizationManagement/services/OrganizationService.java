package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrgSetupValidationResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationTypeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.*;
import java.util.List;

public interface OrganizationService {

    Organization create(Organization organization);
    Organization validate(Organization organizationDto);
    List<OrganizationTypeEntity> getOrgType();
    OrgSetupValidationResponse getValidation(String orgId);
    OrganizationType getUserOrgType(String orgId);
    List<Role> getAllRole(String orgId, String role);
    Privilege addPrivileges(Privilege privilegeModel, String orgId);
    RolePrivilege addRolwisePrivileges(RolePrivilege rolePrivilegeModel, String orgId);
}
