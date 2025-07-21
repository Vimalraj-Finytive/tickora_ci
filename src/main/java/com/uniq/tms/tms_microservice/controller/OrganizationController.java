package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.config.JwtUtil;
import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.OrgSetupValidationResponse;
import com.uniq.tms.tms_microservice.dto.OrganizationDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(UserConstant.ORGANIZATION_URL)
public class OrganizationController {

    private final AuthFacade authFacade;
    private final JwtUtil jwtUtil;

    public OrganizationController(AuthFacade authFacade, AuthFacade authFacade1, JwtUtil jwtUtil) {
        this.authFacade = authFacade1;
        this.jwtUtil = jwtUtil;
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

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Authorization header");
        }

        if (!token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token format");
        }

        String jwt = token.substring(7);
        String orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null || orgId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }

        ApiResponse<OrgSetupValidationResponse> response = authFacade.getValidation(orgId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}

