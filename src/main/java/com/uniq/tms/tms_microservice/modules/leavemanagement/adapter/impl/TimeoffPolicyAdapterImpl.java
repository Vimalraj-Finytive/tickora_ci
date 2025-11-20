package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeoffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.TimeoffPolicyRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.UserPolicyRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TimeoffPolicyAdapterImpl implements TimeoffPolicyAdapter {

    private final TimeoffPolicyRepository policyRepo;
    private final UserPolicyRepository userPolicyRepo;
    private final LeaveBalanceRepository leaveBalanceRepo;
    private final TimeoffPolicyRepository repository;
    private final UserPolicyRepository repo;
    private final UserRepository repos;

  
    public TimeoffPolicyAdapterImpl(TimeoffPolicyRepository repository, UserPolicyRepository repo, UserRepository repos) {
        this.repository = repository;
        this.repo = repo;
        this.repos = repos;
    }
  
    @Override
    public TimeoffPolicyEntity savePolicy(TimeoffPolicyEntity policy) {
        return policyRepo.save(policy);
    }

    @Override
    public TimeoffPolicyEntity findByPolicyId(String policyId) {
        return policyRepo.findByPolicyId(policyId);
    }


    @Override
    public List<TimeoffPolicyEntity> findPoliciesByIds(List<String> policyIds) {

        if (policyIds == null || policyIds.isEmpty()) {
            return Collections.emptyList();
        }
        return policyRepo.findByPolicyIdIn(policyIds);
    }


    public List<TimeoffPolicyEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public TimeoffPolicyEntity findById(String id) {
        return repository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.CONFLICT, "No timeoffpolicy Found"));
    }

    @Override
    public List<String> findUserIdsByPolicyId(String policyId) {
        return repo.findUserIdsByPolicyId(policyId);
    }

    @Override
    public String findUsernameByUserId(String userId) {
        return repos.findUsernameByUserId(userId);
    }
    @Override
    public List<TimeoffPolicyEntity> findByUserId(String userId){
        return repo.findPolicyByUserId(userId);
    }
}

