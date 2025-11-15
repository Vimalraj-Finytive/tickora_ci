package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.*;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrganizationDtoMapper {

    Organization toModel(OrganizationDto organizationDto);

    OrganizationTypeDto toDto(OrganizationType organizationType);

    RoleDto toDto(Role role);

    Privilege toModel(PrivilegeDto privilegeDto);

    PrivilegeDto toDto(Privilege savePrivilege);

    RolePrivilege toModel(RolePrivilegeDto rolePrivilegeDto);

    RolePrivilegeDto toDto(RolePrivilege rolePrivilege);

    OrganizationDetailsDto toDto(OrganizationDetailsModel model);
    List<OrganizationDetailsDto> toOrganizationDtos(List<OrganizationDetailsModel> models);

    List<PlanAnalyticsDto> toPlanAnalyticsDtos(List<PlanAnalyticsModel> models);

    List<OrganizationTypeCountDto> toDto(List<OrganizationTypeCountModel> organizationTypeCountModel);

    List<OrganizationCountResponseDto> toSummaryDto(List<OrganizationCountResponseModel> organizationCount);

}
