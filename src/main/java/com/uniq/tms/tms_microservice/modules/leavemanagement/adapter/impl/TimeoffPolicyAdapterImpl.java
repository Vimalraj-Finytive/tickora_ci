package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeoffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.TimeoffPolicyRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.UserPolicyRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class TimeoffPolicyAdapterImpl implements TimeoffPolicyAdapter {
    private final TimeoffPolicyRepository repository;
    private final UserPolicyRepository repo;
    private final UserRepository repos;

    public TimeoffPolicyAdapterImpl(TimeoffPolicyRepository repository, UserPolicyRepository repo, UserRepository repos) {
        this.repository = repository;
        this.repo = repo;
        this.repos = repos;
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

