package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.LeaveBalanceRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.MonthlySummaryRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.TimeOffPolicyRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserGroupRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class LeaveBalanceAdapterImpl implements LeaveBalanceAdapter {

    private final LeaveBalanceRepository leaveBalanceRepo;
    private final UserGroupRepository userGroupRepository;
    private final TimeOffPolicyRepository timeOffPolicyRepository;
    private final MonthlySummaryRepository monthlySummaryRepository;

    public LeaveBalanceAdapterImpl(LeaveBalanceRepository leaveBalanceRepo, UserGroupRepository userGroupRepository, TimeOffPolicyRepository timeOffPolicyRepository, MonthlySummaryRepository monthlySummaryRepository) {
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.userGroupRepository = userGroupRepository;
        this.timeOffPolicyRepository = timeOffPolicyRepository;
        this.monthlySummaryRepository = monthlySummaryRepository;
    }

    @Override
    public void saveLeaveBalances(List<LeaveBalanceEntity> leaveBalances) {
        leaveBalanceRepo.saveAll(leaveBalances);
    }

    @Override
    public void deleteByPolicyIdAndUserIds(String policyId, Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        leaveBalanceRepo.deleteByPolicyIdAndUserIds(policyId, userIds);
    }
    @Override
    public LeaveBalanceEntity findLeaveBalance(String userId) {
        return leaveBalanceRepo
                .findTopByUser_UserIdOrderByLeaveBalanceIdDesc(userId)
                .orElse(null);
    }

    @Override
    public List<LeaveBalanceEntity> findLeaveBalancesByPolicyId(String policyId) {
        return leaveBalanceRepo.findByPolicyId(policyId);
    }

    @Override
    public List<TimeOffPolicyEntity> findByUserId(String userId) {
        return timeOffPolicyRepository.findPolicyByUserId(userId);
    }


    public List<LeaveBalanceEntity> findBalance(String userId) {
        return leaveBalanceRepo.findLeaveBalanceByUserId( userId);
    }

    @Override
    public List<LeaveBalanceEntity> findBalancesByMonthYearAndAccrualType(int month, int year, AccrualType type) {
        return leaveBalanceRepo.findBalancesByMonthYearAndAccrualType(month, year, type);
    }

    @Override
    public void saveLeaveBalance(LeaveBalanceEntity leaveBalance) {
        leaveBalanceRepo.save(leaveBalance);
    }

    @Override
    public List<LeaveBalanceEntity> findBalancesByYearAndAccrualType(int year, AccrualType type) {
        return leaveBalanceRepo.findBalancesByYearAndAccrualType(year, type);
    }

    @Override
    public LeaveBalanceEntity findForPeriod(String policyId, String userId, LocalDate start, LocalDate end) {
        return leaveBalanceRepo.findForPeriod(policyId, userId, start, end);
    }

    @Override
    public List<LeaveBalanceEntity> findAnnualLeaveBalances(int year, AccrualType accrualType) {
        return leaveBalanceRepo.findAnnualLeaveBalances(year, accrualType);
    }

    @Override
    public void saveAllSummary(List<MonthlySummaryEntity> summaryEntityList) {
        monthlySummaryRepository.saveAll(summaryEntityList);
    }

    @Override
    public List<LeaveBalanceEntity> findAllFixedAccrual(AccrualType type) {
        return leaveBalanceRepo.findAllFixedAccrual(type);
    }
}
