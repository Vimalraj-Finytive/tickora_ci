package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.LeaveBalanceDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(LeaveConstant.LEAVE_BALANCE_URL )
public class LeaveBalanceController{
           private final TimeOffFacade timeOffFacade;
           private final AuthHelper authHelper;

    public LeaveBalanceController(TimeOffFacade timeOffFacade, AuthHelper authHelper) {
        this.timeOffFacade = timeOffFacade;
        this.authHelper = authHelper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getLeaveBalance(
            @RequestHeader("Authorization") String authHeader){
        String userId = authHelper.getUserId();
        ApiResponse<List<LeaveBalanceDto>> balance = timeOffFacade.getLeaveBalance(userId);
        return ResponseEntity.status(balance.getStatusCode()).body(balance);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getLeaveBalance(
            @RequestHeader("Authorization")String token,
            @PathVariable("userId") String userId) {
            ApiResponse<List<LeaveBalanceDto>> response = timeOffFacade.getLeaveBalance(userId);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }



}
