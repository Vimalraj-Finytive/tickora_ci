package com.uniq.tms.tms_microservice.modules.organizationManagement.facade;

import com.uniq.tms.tms_microservice.modules.authenticationManagement.services.AuthService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PlanStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.PaymentDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.PlanDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.OrganizationDtoMapper;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final PaymentDtoMapper paymentDtoMapper;

    public OrganizationFacade(OrganizationService organizationService, SubscriptionService subscriptionService, AuthHelper authHelper, AuthService authService, OrganizationDtoMapper organizationDtoMapper, PaymentService paymentService, PlanDtoMapper planDtoMapper, PaymentDtoMapper paymentDtoMapper) {
        this.organizationService = organizationService;
        this.subscriptionService = subscriptionService;
        this.authHelper = authHelper;
        this.authService = authService;
        this.organizationDtoMapper = organizationDtoMapper;
        this.paymentService = paymentService;
        this.planDtoMapper = planDtoMapper;
        this.paymentDtoMapper = paymentDtoMapper;
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
        if (PlanStatus.EXPIRED.getPlanStatus().equalsIgnoreCase(response.getStatus())) {
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

    public ApiResponse<PaymentDto> getPaymentDetailsBySubscriptionId(String subscriptionId) {

        PaymentDto paymentDetails = paymentService.getPaymentDetailsBySubscriptionId(subscriptionId);

        if (paymentDetails == null) {
            return new ApiResponse<>(
                    204,
                    "No payment details found",
                    null
            );
        }

        return new ApiResponse<>(
                200,
                "Payment details fetched successfully",
                paymentDetails
        );
    }


        public ApiResponse<PlanStatusDto> getCurrentPlanStatus(String orgId) {
        PlanStatusModel model = subscriptionService.getCurrentPlanStatus(orgId);
        PlanStatusDto dto = planDtoMapper.toDto(model);
        return new ApiResponse<>(200, "Plan details fetched successfully", dto);
    }

    public ResponseEntity<byte[]> getPaymentDetailsPdfBySubscriptionId(String subscriptionId, String orgId) {
        return paymentService.getPaymentDetailsPdfBySubscriptionId(subscriptionId, orgId);
    }

    public ApiResponse<List<OrganizationDetailsDto>> getAllOrganizationDetails() {
        List<OrganizationDetailsModel> modelList = organizationService.getAllOrganizationDetails();
        List<OrganizationDetailsDto> dto = organizationDtoMapper.toOrganizationDtos(modelList);
        return new ApiResponse<>(HttpStatus.OK.value(), "Organization Details Fetched Successfully", dto);
    }

    public ApiResponse<CalculatedAmountDto> calculateAmount(int additionalUsers) {
        String orgId = authHelper.getOrgId();
        CalculatedAmountDto responseData = subscriptionService.calculateProratedAmount(additionalUsers, orgId);

        return new ApiResponse<>(
                200,
                "Amount calculated successfully",
                responseData
        );
    }

    public ApiResponse<Void> addSubscribedUsers(UpgradePlanDto dto) {
        String orgId = authHelper.getOrgId();
        String orgSchema = authHelper.getSchema();

        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }

        boolean isUpdated = subscriptionService.addSubscribedUsers(orgId, orgSchema, dto);

        if (isUpdated) {
            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Subscribed users updated successfully.",
                    null
            );
        } else {
            return new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to update subscribed users.",
                    null
            );
        }
    }

    public ApiResponse<List<PlanAnalyticsDto>> getPlanAnalytics(LocalDate fromDate, LocalDate toDate) {
        List<PlanAnalyticsDto> dto = organizationDtoMapper.toPlanAnalyticsDtos(subscriptionService.calculatePlanUsage(fromDate, toDate));
        return  new ApiResponse<>(200,"Fetched Organization Onboard details plans Successfully",dto);
    }

    public  ApiResponse<List<OrganizationCountResponseDto>>  getOrganizationCounts(LocalDateTime from, LocalDateTime to) {
        List<OrganizationCountResponseDto> dto = organizationDtoMapper.toSummaryDto(organizationService.getOrganizationCounts(from,to));
        return  new ApiResponse<>(200,"Fetched Organization Onboard details Summary Successfully",dto);
    }

    public ApiResponse<List<OrganizationTypeCountDto>> getOrganizationTypeCounts(LocalDateTime from, LocalDateTime to) {
        List<OrganizationTypeCountDto> dto = organizationDtoMapper.toDto(organizationService.getOrgCountByOrgType(from, to));
        return new ApiResponse<>(200,"Fetched Organization Onboard details by OrgType Successfully",dto);
    }

    public OrganizationUsageResponseDto getOrganizationUsage(DateRangeRequestDto request) {
        return organizationService.calculateOrganizationUsage(request);
    }

    public ApiResponse updateSubscription(UpdatePlanDto dto) {
        String orgId = authHelper.getOrgId();
        String orgSchema = authHelper.getSchema();
        try {
            boolean result = subscriptionService.updateSubscription(orgId, orgSchema, dto);
            return result
                    ? new ApiResponse<>(200, "Subscription Updated Successfully...", null)
                    : new ApiResponse<>(400, "Subscription Update Failed. Try Again.", null);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, "Invalid Plan Id", null);
        }
    }

    public ApiResponse<List<MonthlyPaymentDto>> getOrganizationsales(int year){
        List<MonthlyPaymentDto> dto = paymentDtoMapper.toMonthlyPaymentDtoList(paymentService.getOrganizationSales(year));
        return  new ApiResponse<>(200,"Fetched Organization Onboard details Sales Successfully",dto);
    }

    public ApiResponse<List<TopCustomersDto>> getOrganizationTopCustomers(int year){
        List<TopCustomersDto> dto  =  paymentDtoMapper.toTopCustomersDtoList(paymentService.getOrganizationTopCustomers(year));
        return  new ApiResponse<>(200,"Fetched Organization Onboard details TopCustomers Successfully",dto);
    }

}
