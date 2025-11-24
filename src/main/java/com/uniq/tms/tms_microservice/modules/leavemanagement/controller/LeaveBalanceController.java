package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.LeaveBalanceDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.UserWithLeaveBalanceDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(LeaveConstant.LEAVEBALANCE_URL )
public class LeaveBalanceController{
           private final TimeOffFacade timeOffFacade;
           private final AuthHelper authHelper;

    public LeaveBalanceController(TimeOffFacade timeOffFacade, AuthHelper authHelper) {
        this.timeOffFacade = timeOffFacade;
        this.authHelper = authHelper;
    }

    @GetMapping("/leave")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getLeaveBalance(
            @RequestHeader("Authorization") String authHeader){
        String userId = authHelper.getUserId();
        ApiResponse<List<LeaveBalanceDto>> balance = timeOffFacade.getLeaveBalance(userId);
        return ResponseEntity.status(balance.getStatusCode()).body(balance);
    }

    @GetMapping("/leave/{userId}")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto>>> getLeaveBalance(
            @RequestHeader("Authorization")String token,
            @PathVariable("userId") String userId) {
            ApiResponse<List<LeaveBalanceDto>> response = timeOffFacade.getLeaveBalance(userId);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }

    @GetMapping("/supervisor/leave")
    public ResponseEntity<ApiResponse<List<UserWithLeaveBalanceDto>>> getLeave(
            @RequestHeader("Authorization") String token) {
        String userId = authHelper.getUserId();
        ApiResponse<List<UserWithLeaveBalanceDto>> response =
                timeOffFacade.getSupervisorLeave(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);

    }

}
