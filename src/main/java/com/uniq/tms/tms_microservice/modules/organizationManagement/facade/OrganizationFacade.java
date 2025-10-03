package com.uniq.tms.tms_microservice.modules.organizationManagement.facade;

import com.uniq.tms.tms_microservice.modules.authenticationManagement.services.AuthService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.OrganizationDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Privilege;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.RolePrivilege;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Organization;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.OrganizationType;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class OrganizationFacade {

    private final OrganizationService organizationService;
    private final AuthHelper authHelper;
    private final AuthService authService;
    private final OrganizationDtoMapper organizationDtoMapper;

    public OrganizationFacade(OrganizationService organizationService, AuthHelper authHelper, AuthService authService, OrganizationDtoMapper organizationDtoMapper) {
        this.organizationService = organizationService;
        this.authHelper = authHelper;
        this.authService = authService;
        this.organizationDtoMapper = organizationDtoMapper;
    }

    public ApiResponse createOrg(OrganizationDto organizationDto) {
        Organization organization = organizationDtoMapper.toModel(organizationDto);
        Organization response = organizationService.create(organization);
        return new ApiResponse(200,"Organization and Superadmin Created Successfully", response);
    }

    public ApiResponse validateOrg(OrganizationDto organizationDto) {
        Organization organization = organizationDtoMapper.toModel(organizationDto);
        Organization response = organizationService.validate(organization);
        return new ApiResponse<>(200,"Valid Organization", response);
    }

    public ApiResponse getOrgType() {
        return new ApiResponse<>(200,"Organization Type Fetched Successfully", organizationService.getOrgType());
    }

    public ApiResponse<OrgSetupValidationResponse> getValidation(String orgId) {
        OrgSetupValidationResponse validationResponse = organizationService.getValidation(orgId);
        return new ApiResponse<>(200, "Validation Successful", validationResponse);
    }

    public ApiResponse<OrganizationTypeDto> getUserOrgType() {

        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        OrganizationType dto = organizationService.getUserOrgType(orgId);
        OrganizationTypeDto response = organizationDtoMapper.toDto(dto);
        return new ApiResponse<>(200, "User Organization Type Fetched Successfully", response);
    }

    public ApiResponse<OrganizationDropdownDto> getDropDowns() {
        OrganizationDropdownDto response = authService.getDropDowns();
        return new ApiResponse<>(200,"DropDowns Fetched Successfully", response);
    }

    public ApiResponse getAllRole(String orgId, String role) {
        List<RoleDto> roles = organizationService.getAllRole(orgId, role).stream().map(organizationDtoMapper::toDto).toList();
        return new ApiResponse(
                200,
                "Roles fetched successfully",
                roles
        );
    }

    public ApiResponse addPrivileges( PrivilegeDto privilegeDto) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        Privilege privilegeModel = organizationDtoMapper.toModel(privilegeDto);
        Privilege savePrivilege = organizationService.addPrivileges(privilegeModel, orgId);
        PrivilegeDto dto = organizationDtoMapper.toDto(savePrivilege);
        return new ApiResponse(200, "Privilege added successfully", dto);
    }

    public ApiResponse addRolwisePrivileges( RolePrivilegeDto rolePrivilegeDto) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        RolePrivilege rolePrivilegeModel = organizationDtoMapper.toModel(rolePrivilegeDto);
        RolePrivilege savePrivilege = organizationService.addRolwisePrivileges(rolePrivilegeModel, orgId);
        RolePrivilegeDto dto = organizationDtoMapper.toDto(savePrivilege);
        return new ApiResponse(200, "Privilege added successfully", dto);
    }
}
