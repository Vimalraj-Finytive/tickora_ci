package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import java.util.List;
import java.time.LocalDate;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel;

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
}
