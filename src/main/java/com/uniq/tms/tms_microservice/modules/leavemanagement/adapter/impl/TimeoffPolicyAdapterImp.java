package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeoffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.LeaveBalanceRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.TimeoffPolicyRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.TimeoffRequestRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.UsersRequestMappingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TimeoffPolicyAdapterImp implements TimeoffPolicyAdapter {

    private final TimeoffPolicyRepository timeoffPolicyRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final TimeoffRequestRepository timeoffRequestRepository;
    private final UsersRequestMappingRepository usersRequestMappingRepository;

    public TimeoffPolicyAdapterImp(TimeoffPolicyRepository timeoffPolicyRepository, LeaveBalanceRepository leaveBalanceRepository, TimeoffRequestRepository timeoffRequestRepository, UsersRequestMappingRepository usersRequestMappingRepository) {
        this.timeoffPolicyRepository = timeoffPolicyRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.timeoffRequestRepository = timeoffRequestRepository;
        this.usersRequestMappingRepository = usersRequestMappingRepository;
    }

    @Override
    public TimeoffPolicyEntity findPolicyById(String policyId) {
        return timeoffPolicyRepository.findById(policyId).get();
    }

    @Override
    public LeaveBalanceEntity findLeaveBalance(String policyId, String userId) {
        return leaveBalanceRepository.findByPolicy_PolicyIdAndUserId(policyId, userId);
    }

    @Override
    public boolean existsTimeoffRequest(String userId, String policyId) {
        return timeoffRequestRepository.existsByUserIdAndPolicy_PolicyId(userId, policyId);
    }

    @Override
    public TimeoffRequestEntity saveRequest(TimeoffRequestEntity entity) {
        return timeoffRequestRepository.save(entity);
    }

    @Override
    public List<UsersRequestMappingEntity> saveUsersRequestMapping(List<UsersRequestMappingEntity> entity) {
        return usersRequestMappingRepository.saveAll(entity);
    }

    @Override
    public TimeoffRequestEntity findByUserIdAndRequestDate(String userId, LocalDate requestDate) {
        return timeoffRequestRepository.findByUserIdAndRequestDate(userId, requestDate);
    }

    @Override
    public List<TimeoffRequestEntity> saveAllRequest(List<TimeoffRequestEntity> entities) {
        return timeoffRequestRepository.saveAll(entities);
    }

    @Override
    public List<TimeoffRequestEntity> findStartByDate(LocalDate date) {
        return timeoffRequestRepository.findByStartDate(date);
    }

    @Override
    public List<LeaveBalanceEntity> saveAllLeaveBalance(List<LeaveBalanceEntity> entities) {
        return leaveBalanceRepository.saveAll(entities);
    }
}
