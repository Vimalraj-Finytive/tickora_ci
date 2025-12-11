package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UserPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserPolicyAdapter {

    void saveUserPolicies(List<UserPolicyEntity> userPolicies);

    Map<String, String> findUserPolicyMap(List<String> userIds);

    List<UserPolicyEntity> findUserPolicyEntities(List<String> userIds);

    void deleteByPolicyIdAndUserIds(String policyId, Set<String> userIds);

    List<UserPolicyEntity> findUserPoliciesByPolicyId(String policyId);

    Optional<UserPolicyEntity> findByUserIdAndPolicyId(String userId, String policyId);

    List<UserPolicyEntity> findAllByPolicyIdsAndUserIds(List<String> policyIds, Set<String> userIds);

    boolean isUserPolicyActive(String policyId, String userId,  LocalDate startDate, LocalDate endDate);

    List<String> findAllUserIdsInUserPolicies(LocalDate date);


    List<UserPolicyEntity> findByUserIdAndAccrualType(String userId, AccrualType accrualType);

    void deleteById(Long id);

    List<UserPolicyEntity> findUserPoliciesByUserId(String userId);

    List<UserPolicyEntity> findActivePoliciesByUserId(String userId);

    UserPolicyEntity saveUserPolicy(UserPolicyEntity entity);


    List<UserPolicyProjection> findUserPolicyValidTo(AccrualType type);

    List<String> findUserIdsByPolicyId(String policyId);

}
