package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.OrgSetupValidationResponse;
import com.uniq.tms.tms_microservice.entity.OrganizationTypeEntity;
import com.uniq.tms.tms_microservice.model.Organization;
import com.uniq.tms.tms_microservice.model.OrganizationType;
import java.util.List;

public interface OrganizationService {

    Organization create(Organization organization);
    Organization validate(Organization organizationDto);
    List<OrganizationTypeEntity> getOrgType();
    OrgSetupValidationResponse getValidation(String orgId);
    OrganizationType getUserOrgType(String orgId);
}
