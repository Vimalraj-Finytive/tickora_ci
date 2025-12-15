package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.LeaveBalanceRepository;
import org.springframework.stereotype.Component;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.TimeOffPolicyRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.UserPolicyRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

@Component
public class TimeoffPolicyAdapterImpl implements TimeOffPolicyAdapter {

    private final TimeOffPolicyRepository timeoffPolicyRepo;
    private final UserPolicyRepository userPolicyRepo;
    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public TimeoffPolicyAdapterImpl(TimeOffPolicyRepository timeoffPolicyRepo, UserPolicyRepository userPolicyRepo,
                                    UserRepository userRepository, LeaveBalanceRepository leaveBalanceRepository) {
        this.timeoffPolicyRepo = timeoffPolicyRepo;
        this.userPolicyRepo = userPolicyRepo;
        this.userRepository = userRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Override
    public TimeOffPolicyEntity savePolicy(TimeOffPolicyEntity policy) {
        return timeoffPolicyRepo.save(policy);
    }

    @Override
    public TimeOffPolicyEntity findByPolicyId(String policyId) {
        return timeoffPolicyRepo.findByPolicyIdAndIsActiveTrue(policyId);
    }

    @Override
    public List<TimeOffPolicyEntity> findPoliciesByIds(List<String> policyIds) {

        if (policyIds == null || policyIds.isEmpty()) {
            return Collections.emptyList();
        }
        return timeoffPolicyRepo.findByPolicyIdIn(policyIds);
    }
    @Override
    public List<TimeOffPolicyEntity> findByIsActiveTrue() {
        return timeoffPolicyRepo.findByIsActiveTrue();
    }

    @Override
    public TimeOffPolicyEntity findById(String id) {
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
    public List<TimeOffPolicyEntity> findByUserId(String userId){
        return timeoffPolicyRepo.findPolicyByUserId(userId);
    }

    @Override
    public boolean existsValidPolicy(String policyId,  LocalDate startDate, LocalDate endDate) {
        return timeoffPolicyRepo.existsValidPolicy(policyId, startDate, endDate);
    }

    @Override
    public TimeOffPolicyEntity findPolicyById(String policyId) {
        return timeoffPolicyRepo.findById(policyId).get();
    }


    @Override
    public boolean existsByPolicyNameIgnoreCase(String policyName) {
        return timeoffPolicyRepo.existsByPolicyNameIgnoreCase(policyName);
    }
    @Override
    public TimeOffPolicyEntity findDefaultPolicy(){
        return timeoffPolicyRepo.findByIsDefaultTrue();
    }

    @Override
    public List<TimeOffPolicyEntity> findAllPoliciesByType(AccrualType type) {
        return timeoffPolicyRepo.findByAccrualTypeAndIsActiveTrue(type);
    }

    @Override
    public void saveAllPolicy(List<TimeOffPolicyEntity> policyList) {
        timeoffPolicyRepo.saveAll(policyList);
    }

    @Override
    public boolean isPolicyActive(String policyId) {
        return timeoffPolicyRepo.findActivePolicyById(policyId);
    }

    @Override
    public List<TimeOffPolicyEntity> findPoliciesList() {
        return timeoffPolicyRepo.findAllByIsDefaultFalseAndIsActiveTrue();
    }
}
