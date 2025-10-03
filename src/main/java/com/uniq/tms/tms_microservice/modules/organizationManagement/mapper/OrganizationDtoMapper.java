package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizationDtoMapper {

    Organization toModel(OrganizationDto organizationDto);

    OrganizationTypeDto toDto(OrganizationType organizationType);

    RoleDto toDto(Role role);

    Privilege toModel(PrivilegeDto privilegeDto);

    PrivilegeDto toDto(Privilege savePrivilege);

    RolePrivilege toModel(RolePrivilegeDto rolePrivilegeDto);

    RolePrivilegeDto toDto(RolePrivilege rolePrivilege);

}
