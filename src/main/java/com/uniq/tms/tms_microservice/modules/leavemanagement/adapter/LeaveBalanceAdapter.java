package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;

import java.util.List;

public interface LeaveBalanceAdapter {
    void saveLeaveBalances(List<LeaveBalanceEntity> leaveBalances);

    void deleteLeaveBalances(String policyId);

    LeaveBalanceEntity findLeaveBalance(String userId);

    List<LeaveBalanceEntity> findLeaveBalancesByPolicyId(String policyId);

}
