package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import java.util.List;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;

public interface TimeOffPolicyAdapter {

    TimeOffPolicyEntity findPolicyById(String policyId);
    TimeOffPolicyEntity savePolicy(TimeOffPolicyEntity policy);
    TimeOffPolicyEntity findByPolicyId(String policyId);
    List<TimeOffPolicyEntity> findPoliciesByIds(List<String> policyIds);
    List<TimeOffPolicyEntity> findAll();
    TimeOffPolicyEntity findById(String id);
    List<String> findUserIdsByPolicyId(String policyId);
    String findUsernameByUserId(String userId);
    List<TimeOffPolicyEntity> findByUserId(String userId);
}
