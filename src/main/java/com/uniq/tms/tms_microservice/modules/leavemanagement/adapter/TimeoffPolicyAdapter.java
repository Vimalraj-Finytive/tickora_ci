package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import java.util.List;

public interface TimeoffPolicyAdapter {

    TimeOffPolicyEntity findPolicyById(String policyId);
    TimeOffPolicyEntity savePolicy(TimeOffPolicyEntity policy);
    TimeOffPolicyEntity findByPolicyId(String policyId);
    List<TimeOffPolicyEntity> findPoliciesByIds(List<String> policyIds);
    List<TimeOffPolicyEntity> findAll();
    TimeOffPolicyEntity findById(String id);
    List<String> findUserIdsByPolicyId(String policyId);
    String findUsernameByUserId(String userId);
    List<TimeOffPolicyEntity> findByUserId(String userId);
   List<LeaveBalanceEntity> findBalance(String userId);
}
