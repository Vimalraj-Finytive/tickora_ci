package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import java.util.List;


public interface TimeoffPolicyAdapter {
     List<TimeoffPolicyEntity> findAll();
     TimeoffPolicyEntity findById(String id);
    List<String> findUserIdsByPolicyId(String policyId);
    String findUsernameByUserId(String userId);
    List<TimeoffPolicyEntity> findByUserId(String userId);
}
