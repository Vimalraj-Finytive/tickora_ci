package com.uniq.tms.tms_microservice.modules.organizationManagement.facade;

import com.uniq.tms.tms_microservice.modules.authenticationManagement.services.AuthService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.PlanDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.OrganizationDtoMapper;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Component
public class OrganizationFacade {

    private final OrganizationService organizationService;
    private final SubscriptionService subscriptionService;
    private final AuthHelper authHelper;
    private final AuthService authService;
    private final OrganizationDtoMapper organizationDtoMapper;
    private final PaymentService paymentService;
    private final PlanDtoMapper planDtoMapper;

    public OrganizationFacade(OrganizationService organizationService, SubscriptionService subscriptionService, AuthHelper authHelper, AuthService authService, OrganizationDtoMapper organizationDtoMapper, PaymentService paymentService, PlanDtoMapper planDtoMapper) {
        this.organizationService = organizationService;
        this.subscriptionService = subscriptionService;
        this.authHelper = authHelper;
        this.authService = authService;
        this.organizationDtoMapper = organizationDtoMapper;
        this.paymentService = paymentService;
        this.planDtoMapper = planDtoMapper;
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

    public ApiResponse<OrganizationSummaryDto> getOrgSummary() {
        String orgId = authHelper.getOrgId();
        OrganizationSummaryDto response = organizationService.getOrgSummary(orgId);
        return new ApiResponse<>(HttpStatus.OK.value(), "Organization Summary Fetched Successfully", response);
    }

    public ApiResponse<SubscriptionDto> getActivePlan() {
        String orgId = authHelper.getOrgId();
        SubscriptionDto response = subscriptionService.getActivePlan(orgId);

        if ("EXPIRED".equalsIgnoreCase(response.getStatus())) {
            return new ApiResponse<>(HttpStatus.OK.value(), "No active plan — showing latest expired plan", response);
        }

        return new ApiResponse<>(HttpStatus.OK.value(), "Active subscription fetched successfully", response);

    }

    public ApiResponse<SubscriptionDto> getPlanHistory() {
        String orgId = authHelper.getOrgId();
        List<SubscriptionDto> response = subscriptionService.getPlanHistory(orgId);
        return new ApiResponse<>(HttpStatus.OK.value(), "Subscription History fetched successfully", response);
    }

    public ApiResponse<PlanDto> getAllPlans() {
        List<PlanDto> response = subscriptionService.getAllPlans();
        return new ApiResponse<>(HttpStatus.OK.value(), "Plan fetched successfully", response);
    }

    public ApiResponse<Map<String, Boolean>> amountValidation(String orgId, String orgSchema, UpgradePlanDto request) {
        boolean isValid = subscriptionService.amountValidation(orgId, orgSchema, request);
        Map<String, Boolean> data = Map.of("isValid", isValid);
        String message = isValid
                ? "Amount is matching with the selected plan."
                : "Amount is not matching with the selected plan.";

        HttpStatus status = isValid ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ApiResponse<>(status.value(), message, data);
    }

    public ApiResponse upgradePlan(String orgId, String orgSchema, UpgradePlanDto upgradePlanDto) {
        boolean isUpgrade = subscriptionService.upgradePlan(orgId, orgSchema, upgradePlanDto);
        Map<String, Boolean> data = Map.of("isValid", isUpgrade);
        String message = isUpgrade
                ? "Subscription Upgraded Successfully..."
                : "Subscription Not Upgraded , Please try Again.";

        HttpStatus status = isUpgrade ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ApiResponse<>(status.value(), message, null);
    }

    public PaymentDto getPaymentDetailsBySubscriptionId(String subscriptionId) {
        return paymentService.getPaymentDetailsBySubscriptionId(subscriptionId);
    }

        public ApiResponse<PlanStatusDto> getCurrentPlanStatus(String orgId) {
        PlanStatusModel model = subscriptionService.getCurrentPlanStatus(orgId);
        PlanStatusDto dto = planDtoMapper.toDto(model);
        return new ApiResponse<>(200, "Plan details fetched successfully", dto);
    }
}
