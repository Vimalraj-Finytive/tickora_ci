package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffRequestAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.LeaveBalanceRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.TimeOffRequestRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.UsersRequestMappingRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class TimeOffRequestAdapterImpl implements TimeOffRequestAdapter {


    private final TimeOffRequestRepository timeoffRequestRepo;
    private final UsersRequestMappingRepository usersRequestMappingRepo;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public TimeOffRequestAdapterImpl(TimeOffRequestRepository timeoffRequestRepo, UsersRequestMappingRepository usersRequestMappingRepo, LeaveBalanceRepository leaveBalanceRepository) {
        this.timeoffRequestRepo = timeoffRequestRepo;
        this.usersRequestMappingRepo = usersRequestMappingRepo;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Override
    public LeaveBalanceEntity findLeaveBalance(String policyId, String userId) {
        return leaveBalanceRepository.findByPolicy_PolicyIdAndUser_UserId(policyId, userId);
    }

    @Override
    public boolean existsTimeoffRequest(String userId, String policyId) {
        return timeoffRequestRepo.existsByUserIdAndPolicy_PolicyId(userId, policyId);
    }

    @Override
    public TimeOffRequestEntity saveRequest(TimeOffRequestEntity entity) {
        return timeoffRequestRepo.save(entity);
    }

    @Override
    public List<UsersRequestMappingEntity> saveUsersRequestMapping(List<UsersRequestMappingEntity> entity) {
        return usersRequestMappingRepo.saveAll(entity);
    }

    @Override
    public TimeOffRequestEntity findByUserIdAndRequestDate(String userId, LocalDate requestDate) {
        return timeoffRequestRepo.findByUserIdAndRequestDate(userId, requestDate);
    }

    @Override
    public List<TimeOffRequestEntity> saveAllRequest(List<TimeOffRequestEntity> entities) {
        return timeoffRequestRepo.saveAll(entities);
    }

    @Override
    public List<TimeOffRequestEntity> findStartByDate(LocalDate date) {
        return timeoffRequestRepo.findByStartDate(date);
    }

    @Override
    public List<LeaveBalanceEntity> saveAllLeaveBalance(List<LeaveBalanceEntity> entities) {
        return leaveBalanceRepository.saveAll(entities);
    }

    @Override
    public List<TimeOffRequestUserModel> filterWithUser(LocalDate from, LocalDate to) {
        return timeoffRequestRepo.filterWithUser(from, to);
    }
}
