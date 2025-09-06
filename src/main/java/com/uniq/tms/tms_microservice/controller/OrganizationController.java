package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import com.uniq.tms.tms_microservice.helper.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(UserConstant.ORGANIZATION_URL)
public class OrganizationController {

    private final AuthFacade authFacade;
    private final AuthHelper authHelper;

    public OrganizationController(AuthFacade authFacade, AuthFacade authFacade1, AuthHelper authHelper) {
        this.authFacade = authFacade1;
        this.authHelper = authHelper;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createOrg(@RequestBody OrganizationDto organizationDto){
        ApiResponse response = authFacade.createOrg(organizationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse> validateOrg(@RequestBody OrganizationDto organizationDto){
        ApiResponse response = authFacade.validateOrg(organizationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/orgType")
    public ResponseEntity<ApiResponse> getOrgType(){
        ApiResponse response = authFacade.getOrgType();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/onBoard/validate")
    public ResponseEntity<ApiResponse<OrgSetupValidationResponse>> getOnBoardValidation(
            @RequestHeader("Authorization") String token) {
        String orgId = authHelper.getOrgId();
        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        ApiResponse<OrgSetupValidationResponse> response = authFacade.getValidation(orgId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/user/orgType")
    public ResponseEntity<ApiResponse<OrganizationTypeDto>> getUserOrgType(
            @RequestHeader("Authorization") String token) {

        ApiResponse<OrganizationTypeDto> response = authFacade.getUserOrgType();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getDropDowns")
    public ResponseEntity<ApiResponse<OrganizationDropdownDto>> getDropDowns(){
        ApiResponse<OrganizationDropdownDto> response = authFacade.getDropDowns();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

