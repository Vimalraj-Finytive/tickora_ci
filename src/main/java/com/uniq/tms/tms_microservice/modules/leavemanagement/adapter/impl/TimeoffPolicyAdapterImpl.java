package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeoffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffRequestUserModel;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class TimeoffPolicyAdapterImpl implements TimeoffPolicyAdapter {

    private final TimeoffPolicyRepository timeoffPolicyRepo;
    private final UserPolicyRepository userPolicyRepo;
    private final LeaveBalanceRepository leaveBalanceRepo;
    private final UserRepository userRepository;
    private final TimeoffRequestRepository timeoffRequestRepo;
    private final UsersRequestMappingRepository usersRequestMappingRepo;


    public TimeoffPolicyAdapterImpl(TimeoffPolicyRepository timeoffPolicyRepo, UserPolicyRepository userPolicyRepo, LeaveBalanceRepository leaveBalanceRepo, UserRepository userRepository, TimeoffRequestRepository timeoffRequestRepo, UsersRequestMappingRepository usersRequestMappingRepo) {
        this.timeoffPolicyRepo = timeoffPolicyRepo;
        this.userPolicyRepo = userPolicyRepo;
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.userRepository = userRepository;
        this.timeoffRequestRepo = timeoffRequestRepo;
        this.usersRequestMappingRepo = usersRequestMappingRepo;
    }
  
    @Override
    public TimeoffPolicyEntity savePolicy(TimeoffPolicyEntity policy) {
        return timeoffPolicyRepo.save(policy);
    }

    @Override
    public TimeoffPolicyEntity findByPolicyId(String policyId) {
        return timeoffPolicyRepo.findByPolicyId(policyId);
    }


    @Override
    public List<TimeoffPolicyEntity> findPoliciesByIds(List<String> policyIds) {

        if (policyIds == null || policyIds.isEmpty()) {
            return Collections.emptyList();
        }
        return timeoffPolicyRepo.findByPolicyIdIn(policyIds);
    }


    public List<TimeoffPolicyEntity> findAll() {
        return timeoffPolicyRepo.findAll();
    }

    @Override
    public TimeoffPolicyEntity findById(String id) {
        return timeoffPolicyRepo.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.CONFLICT, "No timeoffpolicy Found"));
    }

    @Override
    public List<String> findUserIdsByPolicyId(String policyId) {
        return userPolicyRepo.findUserIdsByPolicyId(policyId);
    }

    @Override
    public String findUsernameByUserId(String userId) {
        return userRepository.findUsernameByUserId(userId);
    }

    @Override
    public List<TimeoffPolicyEntity> findByUserId(String userId){
        return timeoffPolicyRepo.findPolicyByUserId(userId);
    }

    @Override
    public TimeoffPolicyEntity findPolicyById(String policyId) {
        return timeoffPolicyRepo.findById(policyId).get();
    }

    @Override
    public LeaveBalanceEntity findLeaveBalance(String policyId, String userId) {
        return leaveBalanceRepo.findByPolicy_PolicyIdAndUserId(policyId, userId);
    }

    @Override
    public boolean existsTimeoffRequest(String userId, String policyId) {
        return timeoffRequestRepo.existsByUserIdAndPolicy_PolicyId(userId, policyId);
    }

    @Override
    public TimeoffRequestEntity saveRequest(TimeoffRequestEntity entity) {
        return timeoffRequestRepo.save(entity);
    }

    @Override
    public List<UsersRequestMappingEntity> saveUsersRequestMapping(List<UsersRequestMappingEntity> entity) {
        return usersRequestMappingRepo.saveAll(entity);
    }

    @Override
    public TimeoffRequestEntity findByUserIdAndRequestDate(String userId, LocalDate requestDate) {
        return timeoffRequestRepo.findByUserIdAndRequestDate(userId, requestDate);
    }

    @Override
    public List<TimeoffRequestEntity> saveAllRequest(List<TimeoffRequestEntity> entities) {
        return timeoffRequestRepo.saveAll(entities);
    }

    @Override
    public List<TimeoffRequestEntity> findStartByDate(LocalDate date) {
        return timeoffRequestRepo.findByStartDate(date);
    }

    @Override
    public List<LeaveBalanceEntity> saveAllLeaveBalance(List<LeaveBalanceEntity> entities) {
        return leaveBalanceRepo.saveAll(entities);
    }

    @Override
    public List<TimeoffRequestUserModel> filterWithUser(LocalDate from, LocalDate to) {
        return timeoffRequestRepo.filterWithUser(from, to);
    }

    @Override
    public List<TimeoffRequestUserModel> filterWithUserAndRole(LocalDate from, LocalDate to, int minRoleLevel) {
        return timeoffRequestRepo.filterWithUserAndRole(from, to, minRoleLevel);
    }

}

