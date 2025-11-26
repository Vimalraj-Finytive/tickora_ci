package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;

import java.time.LocalDate;
import java.util.List;

public interface LeaveBalanceAdapter {

    void saveLeaveBalances(List<LeaveBalanceEntity> leaveBalances);
    void deleteLeaveBalances(String policyId);
    LeaveBalanceEntity findLeaveBalance(String userId);
    List<LeaveBalanceEntity> findLeaveBalancesByPolicyId(String policyId);
    List<TimeOffPolicyEntity> findByUserId(String userId);
    List<LeaveBalanceEntity> findBalance(String userId);
    List<LeaveBalanceEntity> findBalancesByMonthYearAndAccrualType(int month, int year, AccrualType type);
    List<LeaveBalanceEntity> findBalancesByYearAndAccrualType(int year, AccrualType type);
    void saveLeaveBalance(LeaveBalanceEntity leaveBalance);
    LeaveBalanceEntity findForPeriod(String policyId, String userId, LocalDate start, LocalDate end);
}
