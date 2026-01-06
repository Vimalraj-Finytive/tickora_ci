package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ResetFrequency;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LeaveBalanceAdapter {

    void saveLeaveBalances(List<LeaveBalanceEntity> leaveBalances);
    void deleteByPolicyIdAndUserIds(String policyId, Set<String> userIds);
    LeaveBalanceEntity findLeaveBalance(String userId);
    List<LeaveBalanceEntity> findLeaveBalancesByPolicyId(String policyId);
    List<TimeOffPolicyEntity> findByUserId(String userId);
    List<LeaveBalanceEntity> findBalance(String userId,String year);
    List<LeaveBalanceEntity> findBalancesByMonthYearAndAccrualType(int month, int year, AccrualType type);
    List<LeaveBalanceEntity> findBalancesByYearAndAccrualType(int year, AccrualType type);
    void saveLeaveBalance(LeaveBalanceEntity leaveBalance);
    LeaveBalanceEntity findForPeriod(String policyId, String userId, LocalDate start, LocalDate end);
    void saveAllSummary(List<MonthlySummaryEntity> summaryEntityList);
    List<LeaveBalanceEntity> findAllFixedAccrual(int month, int year, AccrualType type);
    List<MonthlySummaryEntity> findByMonthAndYear(Integer month, Integer year);
    LeaveBalanceEntity findMonthlyBalance(String userId, LocalDate userFrom);
    LeaveBalanceEntity findAnnualBalance(String userId, LocalDate date);
    LeaveBalanceEntity findByUserIdAndPolicyId(String userId, String policyId);
    LeaveBalanceEntity findActiveBalanceByUserIdAndPolicy(String userId, String policyId);
    Optional<MonthlySummaryEntity> getMonthlySummary(String userId, int month, int year);
    List<LeaveBalanceEntity> findActiveBalances(LocalDate date);
    List<LeaveBalanceEntity> findActiveMonthlyBalances(LocalDate monthStart, LocalDate monthEnd, ResetFrequency frequency, AccrualType accrualType, List<String> userIds);
}
