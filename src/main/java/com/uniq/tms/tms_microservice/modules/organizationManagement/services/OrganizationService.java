package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationTypeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.*;
import java.time.LocalDateTime;
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

    OrganizationSummaryDto getOrgSummary(String orgId);

    List<OrganizationDetailsModel> getAllOrganizationDetails();

//    OrganizationUserCountResponse getUserCountsForOrganization(String orgId, LocalDate fromDate, LocalDate toDate);

    List<OrganizationCountResponseModel> getOrganizationCounts(LocalDateTime from, LocalDateTime to);

    OrganizationUsageResponseDto calculateOrganizationUsage(DateRangeRequestDto request);

    List<OrganizationTypeCountModel> getOrgCountByOrgType(LocalDateTime from, LocalDateTime to);
}
