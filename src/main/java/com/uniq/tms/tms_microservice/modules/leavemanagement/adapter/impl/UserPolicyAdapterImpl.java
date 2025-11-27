package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.UserPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UserPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.UserPolicyRepository;
import org.springframework.stereotype.Component;

import java.util.*;

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
        return userPolicyRepo.findByUserIds(userIds);
    }

    @Override
    public void deleteUserPolicies(String policyId) {
        userPolicyRepo.deleteByPolicyId(policyId);
    }

    @Override
    public List<UserPolicyEntity> findUserPoliciesByPolicyId(String policyId) {
        return userPolicyRepo.findByPolicyId(policyId);
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

}
