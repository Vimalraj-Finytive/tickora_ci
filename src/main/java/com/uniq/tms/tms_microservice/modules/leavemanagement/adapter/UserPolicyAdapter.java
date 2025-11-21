package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UserPolicyEntity;

import java.util.List;
import java.util.Map;

public interface UserPolicyAdapter {

    void saveUserPolicies(List<UserPolicyEntity> userPolicies);

    Map<String, String> findUserPolicyMap(List<String> userIds);

    List<UserPolicyEntity> findUserPolicyEntities(List<String> userIds);

    void deleteUserPolicies(String policyId);

    List<UserPolicyEntity> findUserPoliciesByPolicyId(String policyId);
}
