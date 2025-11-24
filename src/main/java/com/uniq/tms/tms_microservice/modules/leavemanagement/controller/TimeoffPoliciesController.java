package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AdminStatusUpdateDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.EmployeeStatusUpdateDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffPoliciesFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import org.springframework.http.HttpStatus;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AccrualTypeEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CompensationEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPoliciesDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPolicyDto;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(LeaveConstant.TIMEOFFPOLICIES_URL)
public class TimeoffPoliciesController {

    private final TimeOffPoliciesFacade timeOffPoliciesFacade;

    public TimeoffPoliciesController(TimeOffPoliciesFacade timeOffPoliciesFacade) {
        this.timeOffPoliciesFacade = timeOffPoliciesFacade;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createPolicy(@Valid @RequestBody TimeOffPolicyRequestDto request,
                                                          @RequestHeader("Authorization") String token) {
        ApiResponse<Void> response = timeOffPoliciesFacade.createPolicy(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getDropDowns")
    public ResponseEntity<ApiResponse<EntitledTypeDropdownDto>> getDropDowns(@RequestHeader("Authorization") String token) {
        ApiResponse<EntitledTypeDropdownDto> response = timeOffPoliciesFacade.getDropDowns();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/edit")
    public ResponseEntity<ApiResponse<Void>> editPolicy(
            @Valid @RequestBody TimeOffPolicyEditRequestDto request,
            @RequestHeader("Authorization") String token) {
        ApiResponse<Void> response = timeOffPoliciesFacade.editPolicy(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("update/assign")
    public ResponseEntity<ApiResponse<Void>> assignPoliciesToUsers(
            @Valid @RequestBody TimeOffPolicyBulkAssignRequestDto request,
            @RequestHeader("Authorization") String token) {

        ApiResponse<Void> response =timeOffPoliciesFacade.assignPoliciesToUsers(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/status/{policyId}")
    public ResponseEntity<ApiResponse<Void>> inactivatePolicy(
            @PathVariable String policyId,
            @RequestBody TimeOffPolicyInactivateRequestDto request,
            @RequestHeader("Authorization") String token) {

        ApiResponse<Void> response =
                timeOffPoliciesFacade.inactivatePolicy(policyId, request);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/Policies")
    public ResponseEntity<ApiResponse<List<TimeoffPoliciesDto>>> getAllPolicies(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<TimeoffPoliciesDto>> response = timeOffPoliciesFacade.getAllPolicies();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/policy")
    public ResponseEntity<ApiResponse<List<TimeoffPolicyDto>>> getAllPolicy(
            @RequestHeader("Authorization")String token){
    ApiResponse<List<TimeoffPolicyDto>> response   =timeOffPoliciesFacade.getAllPolicy();
    return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/AccrualType")
    public ResponseEntity<ApiResponse<List<AccrualTypeEnumDto>>> getAccrualStatus(
             @RequestHeader("Authorization")String token){
        ApiResponse<List<AccrualTypeEnumDto>> response=timeOffPoliciesFacade.getAccrualStatus();
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }
    @GetMapping("/Compensation")
    public ResponseEntity<ApiResponse<List<CompensationEnumDto>>> getCompensation(
            @RequestHeader("Authorization")String token){
        ApiResponse<List<CompensationEnumDto>> response=timeOffPoliciesFacade.getCompensation();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/policy/{id}")
    public ResponseEntity<ApiResponse<TimeoffPoliciesDto>> getPolicyById(
            @RequestHeader("Authorization")String token,@PathVariable String id){
        ApiResponse<TimeoffPoliciesDto> response=timeOffPoliciesFacade.getPolicyById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/request/create")
    public ResponseEntity<ApiResponse> createRequest(@RequestHeader("Authorization") String token, @RequestBody TimeOffRequestDto requestDto) {
        ApiResponse createdRequest = timeOffPoliciesFacade.createRequest(requestDto);
        return ResponseEntity.ok(createdRequest);
    }

    @PutMapping("/request/update")
    public ResponseEntity<ApiResponse> employeeUpdateStatus(@RequestHeader("Authorization") String token, @RequestBody EmployeeStatusUpdateDto dto){
        ApiResponse updateRequest = timeOffPoliciesFacade.employeeUpdateStatus(dto);
        return ResponseEntity.ok(updateRequest);
    }

    @PatchMapping("/request/update")
    public ResponseEntity<ApiResponse> adminUpdateStatus(@RequestHeader("Authorization") String token, @RequestBody AdminStatusUpdateDto dto){
        ApiResponse updateRequest = timeOffPoliciesFacade.adminUpdateStatus(dto);
        return ResponseEntity.ok(updateRequest);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TimeoffPolicyDto>>> getPoliciesByUserId(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId) {
        ApiResponse<List<TimeoffPolicyDto>> response = timeOffPoliciesFacade.getPolicyByUserId(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/requests/filter/{fromDate}/{toDate}")
    public ResponseEntity<ApiResponse<List<TimeoffRequestResponseDto>>> filterRequests(
            @PathVariable LocalDate fromDate,
            @PathVariable LocalDate toDate) {

        ApiResponse<List<TimeoffRequestResponseDto>> response =
                timeOffPoliciesFacade.filterRequests(fromDate, toDate);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/requests/filter/role/{fromDate}/{toDate}")
    public ResponseEntity<ApiResponse<List<TimeoffRequestResponseDto>>> filterRequestsBasedOnRole(
            @PathVariable LocalDate fromDate,
            @PathVariable LocalDate toDate) {

        ApiResponse<List<TimeoffRequestResponseDto>> response = timeOffPoliciesFacade.filterRequestsBasedOnRole(fromDate, toDate);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


}
