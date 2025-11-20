package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffPoliciesFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AccrualTypeEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CompensationEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPoliciesDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPolicyDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffPoliciesFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(LeaveConstant.TIMEOFFPOLICIES_URL)
public class TimeoffPoliciesController {
       private TimeOffPoliciesFacade facade;

    private final TimeOffPoliciesFacade timeOffPoliciesFacade;

    public TimeoffPoliciesController(TimeOffPoliciesFacade timeOffPoliciesFacade) {
        this.timeOffPoliciesFacade = timeOffPoliciesFacade;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TimeOffPolicyResponseDto>> createPolicy(@RequestBody TimeOffPolicyRequestDto request,
                                                                              @RequestHeader("Authorization") String token) {
        ApiResponse<TimeOffPolicyResponseDto> response = timeOffPoliciesFacade.createPolicy(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getDropDowns")
    public ResponseEntity<ApiResponse<EntitledTypeDropdownDto>> getDropDowns(@RequestHeader("Authorization") String token) {
        ApiResponse<EntitledTypeDropdownDto> response = timeOffPoliciesFacade.getDropDowns();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @PostMapping("/edit")
    public ResponseEntity<ApiResponse<TimeOffPolicyResponseDto>> editPolicy(
            @RequestBody TimeOffPolicyEditRequestDto request,
            @RequestHeader("Authorization") String token) {
        ApiResponse<TimeOffPolicyResponseDto> response = timeOffPoliciesFacade.editPolicy(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("update/assign")
    public ResponseEntity<ApiResponse<String>> assignPoliciesToUsers(
            @RequestBody TimeOffPolicyBulkAssignRequestDto request,
            @RequestHeader("Authorization") String token) {

        timeOffPoliciesFacade.assignPoliciesToUsers(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Policies assigned successfully", null));
    }

    @PatchMapping("/status/{policyId}")
    public ResponseEntity<ApiResponse<String>> inactivatePolicy(
            @PathVariable String policyId,
            @RequestBody TimeOffPolicyInactivateRequestDto request,
            @RequestHeader("Authorization") String token) {

        ApiResponse<String> response =
                timeOffPoliciesFacade.inactivatePolicy(policyId, request);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    public TimeoffPoliciesController(TimeOffPoliciesFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/Policies")
    public ResponseEntity<ApiResponse<List<TimeoffPoliciesDto>>> getAllPolicies(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<TimeoffPoliciesDto>> response = facade.getAllPolicies();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/policy")
    public ResponseEntity<ApiResponse<List<TimeoffPolicyDto>>> getAllPolicy(
            @RequestHeader("Authorization")String token){
    ApiResponse<List<TimeoffPolicyDto>> response   =facade.getAllPolicy();
    return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/AccrualType")
    public ResponseEntity<ApiResponse<List<AccrualTypeEnumDto>>> getAccrualStatus(
             @RequestHeader("Authorization")String token){
        ApiResponse<List<AccrualTypeEnumDto>> response=facade.getAccrualStatus();
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }
    @GetMapping("/Compensation")
    public ResponseEntity<ApiResponse<List<CompensationEnumDto>>> getCompensation(
            @RequestHeader("Authorization")String token){
        ApiResponse<List<CompensationEnumDto>> response=facade.getCompensation();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/policy/{id}")
    public ResponseEntity<ApiResponse<TimeoffPoliciesDto>> getPolicyById(
            @RequestHeader("Authorization")String token,@PathVariable String id){
        ApiResponse<TimeoffPoliciesDto> response=facade.getPolicyById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TimeoffPolicyDto>>> getPoliciesByUserId(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId) {
        ApiResponse<List<TimeoffPolicyDto>> response = facade.getPolicyByUserId(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}