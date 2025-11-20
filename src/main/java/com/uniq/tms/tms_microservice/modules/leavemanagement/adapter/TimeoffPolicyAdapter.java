package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.*;
import java.util.List;
import java.util.Map;

public interface TimeoffPolicyAdapter {
    TimeoffPolicyEntity savePolicy(TimeoffPolicyEntity policy);
    TimeoffPolicyEntity findByPolicyId(String policyId);
    List<TimeoffPolicyEntity> findPoliciesByIds(List<String> policyIds);
    List<TimeoffPolicyEntity> findAll();
    TimeoffPolicyEntity findById(String id);
    List<String> findUserIdsByPolicyId(String policyId);
    String findUsernameByUserId(String userId);
    List<TimeoffPolicyEntity> findByUserId(String userId);
}
