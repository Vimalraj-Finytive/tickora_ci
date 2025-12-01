package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import io.lettuce.core.dynamic.annotation.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface LeaveBalanceAdapter {

    void saveLeaveBalances(List<LeaveBalanceEntity> leaveBalances);
    void deleteByPolicyIdAndUserIds(String policyId, Set<String> userIds);
    LeaveBalanceEntity findLeaveBalance(String userId);
    List<LeaveBalanceEntity> findLeaveBalancesByPolicyId(String policyId);
    List<TimeOffPolicyEntity> findByUserId(String userId);
    List<LeaveBalanceEntity> findBalance(String userId);
    List<LeaveBalanceEntity> findBalancesByMonthYearAndAccrualType(int month, int year, AccrualType type);
    List<LeaveBalanceEntity> findBalancesByYearAndAccrualType(int year, AccrualType type);
    void saveLeaveBalance(LeaveBalanceEntity leaveBalance);
    LeaveBalanceEntity findForPeriod(String policyId, String userId, LocalDate start, LocalDate end);
    List<LeaveBalanceEntity> findAnnualLeaveBalances(int year, AccrualType accrualType);
    void saveAllSummary(List<MonthlySummaryEntity> summaryEntityList);
    List<LeaveBalanceEntity> findAllFixedAccrual(int month, int year, AccrualType type);
    List<MonthlySummaryEntity> findByMonthAndYear(Integer month, Integer year);
}
