package com.uniq.tms.tms_microservice.modules.organizationManagement.controller;

import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.constant.OrganizationConstant;
import com.uniq.tms.tms_microservice.modules.organizationManagement.facade.OrganizationFacade;
import com.uniq.tms.tms_microservice.shared.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(OrganizationConstant.ORGANIZATION_URL)
public class OrganizationController {

    private final OrganizationFacade organizationFacade;
    private final AuthHelper authHelper;

    public OrganizationController(OrganizationFacade organizationFacade, AuthHelper authHelper) {
        this.organizationFacade = organizationFacade;
        this.authHelper = authHelper;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createOrg(@RequestBody OrganizationDto organizationDto) {
        ApiResponse response = organizationFacade.createOrg(organizationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse> validateOrg(@RequestBody OrganizationDto organizationDto) {
        ApiResponse response = organizationFacade.validateOrg(organizationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/orgType")
    public ResponseEntity<ApiResponse> getOrgType() {
        ApiResponse response = organizationFacade.getOrgType();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/onBoard/validate")
    public ResponseEntity<ApiResponse<OrgSetupValidationResponse>> getOnBoardValidation(
            @RequestHeader("Authorization") String token) {
        String orgId = authHelper.getOrgId();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        ApiResponse<OrgSetupValidationResponse> response = organizationFacade.getValidation(orgId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/user/orgType")
    public ResponseEntity<ApiResponse<OrganizationTypeDto>> getUserOrgType(
            @RequestHeader("Authorization") String token) {

        ApiResponse<OrganizationTypeDto> response = organizationFacade.getUserOrgType();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/getDropDowns")
    public ResponseEntity<ApiResponse<OrganizationDropdownDto>> getDropDowns() {
        ApiResponse<OrganizationDropdownDto> response = organizationFacade.getDropDowns();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/role")
    public ResponseEntity<ApiResponse> getAllRole(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Logger logger = LoggerFactory.getLogger(getClass());
        try {
            String orgId = authHelper.getOrgId();
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401, "Unauthorized - Invalid Organization", null));
            }
            String role = authHelper.getRole();
            return ResponseEntity.ok(organizationFacade.getAllRole(orgId, role));
        } catch (RuntimeException e) {
            logger.error("JWT Processing Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
        }
    }

    @PostMapping("/addPrivileges")
    public ResponseEntity<ApiResponse> addPrivileges(@RequestHeader("Authorization") String token,
                                                     @RequestBody PrivilegeDto privilegeDto) {
        ApiResponse response = organizationFacade.addPrivileges(privilegeDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/addRolwisePrivileges")
    public ResponseEntity<ApiResponse> addRolwisePrivileges(@RequestHeader("Authorization") String token,
                                                            @RequestBody RolePrivilegeDto rolePrivilegeDto) {
        ApiResponse response = organizationFacade.addRolwisePrivileges(rolePrivilegeDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getOrganizationSummary(
            @RequestHeader("Authorization") String token) {
        String orgId = authHelper.getOrgId();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return ResponseEntity.ok(organizationFacade.getOrgSummary());
    }

    @GetMapping("/subscription/current")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getActivePlan(
            @RequestHeader("Authorization") String token) {
        String orgId = authHelper.getOrgId();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return ResponseEntity.ok(organizationFacade.getActivePlan());
    }

    @GetMapping("/subscription/history")
    public ResponseEntity<ApiResponse> getPlanHistory(
            @RequestHeader("Authorization") String token) {
        String orgId = authHelper.getOrgId();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return ResponseEntity.ok(organizationFacade.getPlanHistory());
    }

    @GetMapping("/subscription/plans")
    public ResponseEntity<ApiResponse> getAllPlans(
            @RequestHeader("Authorization") String token) {
        String orgId = authHelper.getOrgId();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return ResponseEntity.ok(organizationFacade.getAllPlans());
    }

    @PostMapping("/subscription/payment-validation")
    public ResponseEntity<ApiResponse> amountValidation(
            @RequestHeader("Authorization") String token,
            @RequestBody UpgradePlanDto upgradePlanDto) {
        String orgId = authHelper.getOrgId();
        String orgSchema = authHelper.getSchema();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return ResponseEntity.ok(organizationFacade.amountValidation(orgId, orgSchema, upgradePlanDto));
    }

    @PostMapping("/subscription/upgrade")
    public ResponseEntity<ApiResponse> upgradePlan(
            @RequestHeader("Authorization") String token,
            @RequestBody UpgradePlanDto upgradePlanDto) {
        String orgId = authHelper.getOrgId();
        String orgSchema = authHelper.getSchema();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return ResponseEntity.ok(organizationFacade.upgradePlan(orgId, orgSchema, upgradePlanDto));
    }


    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<PaymentDto> getPaymentDetailsBySubscriptionId(@RequestHeader("Authorization") String token,@PathVariable String subscriptionId) {
        PaymentDto paymentDetails = organizationFacade.getPaymentDetailsBySubscriptionId(subscriptionId);

        if (paymentDetails == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(paymentDetails);
    }

    @GetMapping("subscription/notification")
    public ResponseEntity<ApiResponse<PlanStatusDto>> getCurrentPlan(@RequestHeader("Authorization") String token) {
        String orgId = authHelper.getOrgId();
        ApiResponse<PlanStatusDto> response = organizationFacade.getCurrentPlanStatus(orgId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/invoice/{subscriptionId}")
    public ResponseEntity<byte[]> getPaymentDetailsPdfBySubscriptionId(
            @RequestHeader("Authorization") String token,
            @PathVariable String subscriptionId) {
        String orgId = authHelper.getOrgId();
        return organizationFacade.getPaymentDetailsPdfBySubscriptionId(subscriptionId,orgId);
    }

    @GetMapping("/analytics/orgDetails")
    public ResponseEntity<ApiResponse<List<OrganizationDetailsDto>>> getAllOrgDetails(@RequestHeader ("Authorization") String token) {
        ApiResponse<List<OrganizationDetailsDto>> response = organizationFacade.getAllOrganizationDetails();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


//    @PostMapping("/subscription/upgrade/amount")
//    public ResponseEntity<Double> upgradePlan(@RequestParam int additionalUsers) {
//        double totalAmount = organizationFacade.calculateProratedAmount(additionalUsers);
//        return ResponseEntity.ok(totalAmount);
//    }

//    @PostMapping("/subscription/add-users")
//    public ResponseEntity<ApiResponse> addSubscribedUsers(
//            @RequestHeader("Authorization") String token,
//            @RequestBody UpgradePlanDto upgradePlanDto) {
//        return ResponseEntity.ok(organizationFacade.addSubscribedUsers(upgradePlanDto));
//    }


//    @PostMapping("analytics/user-count")
//    public OrganizationUserCountResponse getUserStatus(@RequestBody UserCountRequest request) {
//        return organizationFacade.getUserCounts(
//                request.getOrgId(),
//                request.getFromDate(),
//                request.getToDate()
//        );
//    }

    @PostMapping("analytics/onboardCount/plans")
    public ResponseEntity<ApiResponse<List<PlanAnalyticsDto>>> getPlanSummary(@RequestHeader("Authorization") String token,
                                                                   @RequestBody DateRangeRequestDto request) {
        ApiResponse<List<PlanAnalyticsDto>> analytics = organizationFacade.getPlanAnalytics(request.getFromDate(), request.getToDate());
        return ResponseEntity.status(analytics.getStatusCode()).body(analytics);
    }

    @PostMapping("analytics/onboardCount/summary")
    public ResponseEntity<ApiResponse<List<OrganizationCountResponseDto>>> getOrganizationCount(
            @RequestHeader("Authorization") String token,
            @RequestBody DateRangeRequestDto dateRange) {
        LocalDateTime from = DateTimeUtil.toStartDate(dateRange.getFromDate());
        LocalDateTime to = DateTimeUtil.toEndDate(dateRange.getToDate());
        ApiResponse<List<OrganizationCountResponseDto>> response = organizationFacade.getOrganizationCounts(from,to);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("analytics/onboardCount/orgType")
    public ResponseEntity<ApiResponse<List<OrganizationTypeCountDto>>> getOrganizationTypeCounts(
            @RequestHeader("Authorization") String token,
            @RequestBody DateRangeRequestDto dateRange) {
        LocalDateTime from = DateTimeUtil.toStartDate(dateRange.getFromDate());
        LocalDateTime to = DateTimeUtil.toEndDate(dateRange.getToDate());
        ApiResponse<List<OrganizationTypeCountDto>> typeCounts = organizationFacade.getOrganizationTypeCounts(from, to);
        return ResponseEntity.status(typeCounts.getStatusCode()).body(typeCounts);
    }

    @PostMapping("analytics/organization-users-usage")
    public ResponseEntity<OrganizationUsageResponseDto> getOrganizationUsage(@RequestBody DateRangeRequestDto request) {
        return ResponseEntity.ok(organizationFacade.getOrganizationUsage(request));
    }

    @GetMapping("analytics/onboardCount/sales")
    public ResponseEntity<ApiResponse<List<MonthlyPaymentDto>>> getOrganizationSales(@RequestHeader("Authorization") String token,
                                                       @RequestParam("year")int year){
        ApiResponse<List<MonthlyPaymentDto>> sales = organizationFacade.getOrganizationsales(year);
        return ResponseEntity.status(sales.getStatusCode()).body(sales);
    }

    @GetMapping("analytics/onboardCount/topCustomers")
    public ResponseEntity<ApiResponse<List<TopCustomersDto>>> getOrganizationTopCustomers(@RequestHeader("Authorization") String token,
                                     @RequestParam("year")int year){
        ApiResponse<List<TopCustomersDto>> topCustomers= organizationFacade.getOrganizationTopCustomers(year);
        return ResponseEntity.status(topCustomers.getStatusCode()).body(topCustomers);
    }

}

