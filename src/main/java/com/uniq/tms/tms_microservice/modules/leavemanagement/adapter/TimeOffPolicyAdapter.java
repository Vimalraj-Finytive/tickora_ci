package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import java.time.LocalDate;
import java.util.List;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyProjection;

public interface TimeOffPolicyAdapter {

    TimeOffPolicyEntity findPolicyById(String policyId);
    TimeOffPolicyEntity savePolicy(TimeOffPolicyEntity policy);
    TimeOffPolicyEntity findByPolicyId(String policyId);
    List<TimeOffPolicyEntity> findPoliciesByIds(List<String> policyIds);
    List<TimeOffPolicyEntity> findByIsActiveTrue();
    TimeOffPolicyEntity findById(String id);
    List<String> findUserIdsByPolicyId(String policyId);
    String findUsernameByUserId(String userId);
    List<TimeOffPolicyEntity> findByUserId(String userId);
    boolean existsValidPolicy(String policyId,  LocalDate startDate, LocalDate endDate);
    boolean existsByPolicyNameIgnoreCase(String policyName);
    TimeOffPolicyEntity findDefaultPolicy();
    List<TimeOffPolicyEntity> findAllPoliciesByType(AccrualType type);
    void saveAllPolicy(List<TimeOffPolicyEntity> policyList);
    boolean isPolicyActive(String policyId);
}
