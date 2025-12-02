package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import org.springframework.http.HttpStatus;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AccrualTypeEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CompensationEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPoliciesDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPolicyDto;
import java.util.List;

@RestController
@RequestMapping(LeaveConstant.TIMEOFF_POLICIES_URL)
public class TimeOffPoliciesController {

    private final TimeOffFacade timeOffFacade;
    private final AuthHelper authHelper;

    public TimeOffPoliciesController(TimeOffFacade timeOffFacade, AuthHelper authHelper) {
        this.timeOffFacade = timeOffFacade;
        this.authHelper = authHelper;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createPolicy(@RequestBody TimeOffPolicyRequestDto request,
                                                          @RequestHeader("Authorization") String token) {
        ApiResponse<Void> response = timeOffFacade.createPolicy(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/entitleType")
    public ResponseEntity<ApiResponse<EntitledTypeDropdownDto>> getDropDowns(@RequestHeader("Authorization") String token) {
        ApiResponse<EntitledTypeDropdownDto> response = timeOffFacade.getDropDowns();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Void>> editPolicy(
            @RequestBody TimeOffPolicyEditRequestDto request,
            @RequestHeader("Authorization") String token) {
        ApiResponse<Void> response = timeOffFacade.editPolicy(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<Void>> assignPoliciesToUsers(
            @RequestBody TimeOffPolicyBulkAssignRequestDto request,
            @RequestHeader("Authorization") String token) {
        ApiResponse<Void> response =  timeOffFacade.assignPoliciesToUsers(request);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/{policyId}/status")
    public ResponseEntity<ApiResponse<Void>> inactivatePolicy(
            @PathVariable String policyId,
            @RequestBody TimeOffPolicyInactivateRequestDto request,
            @RequestHeader("Authorization") String token) {
        ApiResponse<Void> response =
                timeOffFacade.inactivatePolicy(policyId, request);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeoffPoliciesDto>>> getAllPolicies(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<TimeoffPoliciesDto>> response = timeOffFacade.getAllPolicies();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/basic")
    public ResponseEntity<ApiResponse<List<TimeoffPolicyDto>>> getAllPolicy(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<TimeoffPolicyDto>> response = timeOffFacade.getAllPolicy();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/accrualType")
    public ResponseEntity<ApiResponse<List<AccrualTypeEnumDto>>> getAccrualStatus(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<AccrualTypeEnumDto>> response = timeOffFacade.getAccrualStatus();
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }

    @GetMapping("/compensation")
    public ResponseEntity<ApiResponse<List<CompensationEnumDto>>> getCompensation(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<CompensationEnumDto>> response = timeOffFacade.getCompensation();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeoffPoliciesDto>> getPolicyById(
            @RequestHeader("Authorization") String token, @PathVariable String id) {
        ApiResponse<TimeoffPoliciesDto> response = timeOffFacade.getPolicyById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TimeoffPolicyDto>>> getPoliciesByUserId(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId) {
        ApiResponse<List<TimeoffPolicyDto>> response = timeOffFacade.getPolicyByUserId(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<TimeoffPolicyDto>>> getPoliciesByUserId(
            @RequestHeader("Authorization") String token) {
        String userId = authHelper.getUserId();
        ApiResponse<List<TimeoffPolicyDto>> response = timeOffFacade.getPolicyByUserId(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/resetFrequency")
    public ResponseEntity<ApiResponse<List<ResetFrequencyEnumDto>>> getResetFrequency(
            @RequestHeader("Authorization") String token) {

        ApiResponse<List<ResetFrequencyEnumDto>> response = timeOffFacade.getResetFrequencyStatus();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}