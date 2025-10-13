package com.uniq.tms.tms_microservice.modules.organizationManagement.controller;

import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.constant.OrganizationConstant;
import com.uniq.tms.tms_microservice.modules.organizationManagement.facade.OrganizationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<ApiResponse> createOrg(@RequestBody OrganizationDto organizationDto){
        ApiResponse response = organizationFacade.createOrg(organizationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse> validateOrg(@RequestBody OrganizationDto organizationDto){
        ApiResponse response = organizationFacade.validateOrg(organizationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/orgType")
    public ResponseEntity<ApiResponse> getOrgType(){
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
    public ResponseEntity<ApiResponse<OrganizationDropdownDto>> getDropDowns(){
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
        ApiResponse response = organizationFacade.addPrivileges( privilegeDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/addRolwisePrivileges")
    public ResponseEntity<ApiResponse> addRolwisePrivileges(@RequestHeader("Authorization") String token,
                                                            @RequestBody RolePrivilegeDto rolePrivilegeDto) {
        ApiResponse response = organizationFacade.addRolwisePrivileges( rolePrivilegeDto);
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
    public ResponseEntity<ApiResponse> getActivePlan(
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

    @PostMapping("/subscription/upgradePlan")
    public ResponseEntity<ApiResponse> upgradePlan(
            @RequestHeader("Authorization") String token,
            @RequestBody UpgradePlanDto upgradePlanDto) {
        String orgId = authHelper.getOrgId();
        String orgSchema = authHelper.getSchema();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return ResponseEntity.ok( organizationFacade.upgradePlan(orgId,orgSchema, upgradePlanDto));
    }
    @GetMapping("subscription/planDetails")
    public ResponseEntity<ApiResponse<PlanStatusDto>> getCurrentPlan(@RequestHeader("Authorization") String token) {
        String orgId = authHelper.getOrgId(); // extract from token
        ApiResponse<PlanStatusDto> response = organizationFacade.getCurrentPlanStatus(orgId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

