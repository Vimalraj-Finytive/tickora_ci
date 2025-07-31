package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.OrgSetupValidationResponse;
import com.uniq.tms.tms_microservice.dto.OrganizationDto;
import com.uniq.tms.tms_microservice.dto.OrganizationTypeDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import com.uniq.tms.tms_microservice.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(UserConstant.ORGANIZATION_URL)
public class OrganizationController {

    private final AuthFacade authFacade;
    private final AuthUtil authUtil;

    public OrganizationController(AuthFacade authFacade, AuthFacade authFacade1, AuthUtil authUtil) {
        this.authFacade = authFacade1;
        this.authUtil = authUtil;
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
        String orgId = authUtil.getOrgId();
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
}

