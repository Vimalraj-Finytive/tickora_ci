package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
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
