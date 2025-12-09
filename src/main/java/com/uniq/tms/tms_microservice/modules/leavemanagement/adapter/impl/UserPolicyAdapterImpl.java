package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.UserPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UserPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyProjection;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.UserPolicyRepository;
import org.springframework.stereotype.Component;
import java.util.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserPolicyAdapterImpl implements UserPolicyAdapter {
   private final UserPolicyRepository userPolicyRepo;

    public UserPolicyAdapterImpl(UserPolicyRepository userPolicyRepo) {
        this.userPolicyRepo = userPolicyRepo;
    }

    @Override
    public void saveUserPolicies(List<UserPolicyEntity> userPolicies) {
        userPolicyRepo.saveAll(userPolicies);
    }


    @Override
    public Map<String, String> findUserPolicyMap(List<String> userIds) {
        List<Object[]> rows = userPolicyRepo.findUserPolicyMap(userIds);

        Map<String, String> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (String) row[1]);
        }
        return map;
    }

    @Override
    public List<UserPolicyEntity> findUserPolicyEntities(List<String> userIds) {
        return userPolicyRepo.findByUser_UserIds(userIds);
    }

    @Override
    public void deleteByPolicyIdAndUserIds(String policyId, Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        userPolicyRepo.deleteByPolicyIdAndUserIds(policyId, userIds);
    }
    @Override
    public List<UserPolicyEntity> findUserPoliciesByPolicyId(String policyId) {
        return userPolicyRepo.findByPolicyId(policyId);
    }

    @Override
    public boolean isUserPolicyActive(String policyId, String userId,  LocalDate startDate, LocalDate endDate) {
        return userPolicyRepo.isUserPolicyActive(policyId, userId, startDate, endDate);
    }

    @Override
    public List<String> findAllUserIdsInUserPolicies(LocalDate date) {
        return userPolicyRepo.findAllUserIdsInUserPolicies(date);
    }

    @Override
    public Optional<UserPolicyEntity> findByUserIdAndPolicyId(String userId, String policyId) {

        List<UserPolicyEntity> list = userPolicyRepo.findAllByUserAndPolicy(userId, policyId);

        if (list == null || list.isEmpty())
            return Optional.empty();

        return Optional.of(list.getFirst());
    }

    @Override
    public List<UserPolicyEntity> findAllByPolicyIdsAndUserIds(List<String> policyIds, Set<String> userIds) {
        return userPolicyRepo.findAllByPolicyIdsAndUserIds(policyIds, userIds);
    }

    @Override
    public List<UserPolicyEntity> findByUserIdAndAccrualType(String userId, AccrualType accrualType) {
        return userPolicyRepo.findByUser_UserIdAndPolicy_AccrualType(userId, accrualType);
    }

    @Override
    public void deleteById(Long id) {
        userPolicyRepo.deleteById(id);
    }

    @Override
    public List<UserPolicyEntity> findUserPoliciesByUserId(String userId) {
        return userPolicyRepo.findByUser_UserIdAndActiveTrue(userId);
    }

    @Override
    public List<UserPolicyProjection> findUserPolicyValidTo(AccrualType type) {
        return userPolicyRepo.findUserPolicyValidTo(type);
    }

    @Override
    public List<UserPolicyEntity> findActivePoliciesByUserId(String userId) {
        return userPolicyRepo.findActivePoliciesByUserId(userId);
    }

    @Override
    public UserPolicyEntity saveUserPolicy(UserPolicyEntity entity) {
        return userPolicyRepo.save(entity);
    }

}
