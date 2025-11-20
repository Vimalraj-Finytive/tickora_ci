package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaveBalanceAdapterImpl implements LeaveBalanceAdapter {
    private final LeaveBalanceRepository leaveBalanceRepo;

    public LeaveBalanceAdapterImpl(LeaveBalanceRepository leaveBalanceRepo) {
        this.leaveBalanceRepo = leaveBalanceRepo;
    }

    @Override
    public void saveLeaveBalances(List<LeaveBalanceEntity> leaveBalances) {
        leaveBalanceRepo.saveAll(leaveBalances);
    }


    @Override
    public void deleteLeaveBalances(String policyId) {
        leaveBalanceRepo.deleteByPolicyId(policyId);
    }

    @Override
    public LeaveBalanceEntity findLeaveBalance(String userId) {
        return leaveBalanceRepo
                .findTopByUserIdOrderByLeaveBalanceIdDesc(userId)
                .orElse(null);
    }

    @Override
    public List<LeaveBalanceEntity> findLeaveBalancesByPolicyId(String policyId) {
        return leaveBalanceRepo.findByPolicyId(policyId);
    }
}
