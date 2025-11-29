package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffRequestAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
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
    public TimeOffRequestEntity saveRequest(TimeOffRequestEntity entity) {
        return timeoffRequestRepo.save(entity);
    }

    @Override
    public List<UsersRequestMappingEntity> saveUsersRequestMapping(List<UsersRequestMappingEntity> entity) {
        return usersRequestMappingRepo.saveAll(entity);
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
    public List<TimeOffRequestUserModel> filterWithUser(LocalDate from, LocalDate to, String userId) {
        return timeoffRequestRepo.filterWithUser(from, to,userId);
    }
    @Override
    public List<TimeOffRequestUserModel> filterCreatedByUser(LocalDate from, LocalDate to, String userId) {
        return timeoffRequestRepo.filterCreatedByUser(from, to,userId);
    }

    @Override
    public List<TimeOffRequestUserModel> filterWithUserAndRole(LocalDate from, LocalDate to, int minRoleLevel) {
        return timeoffRequestRepo.filterWithUserAndRole(from, to, minRoleLevel);
    }

    @Override
    public TimeOffRequestEntity getTimeoffRequest(String policyId, String userId, LocalDate requestDate) {
        return timeoffRequestRepo.findByUser_UserIdAndRequestDate(policyId, userId, requestDate);
    }

    @Override
    public boolean existsTimeoffRequest(String userId, String policyId, LocalDate requestDate) {
        return timeoffRequestRepo.existsTimeoffRequest(userId, policyId, requestDate);
    }

    @Override
    public List<TimeOffRequestEntity> findByStartDate(LocalDate date) {
        return timeoffRequestRepo.findByStartDateAndStatusApproved(date);
    }

    @Override
    public List<TimeOffRequestEntity> findAllUnpaidRequest(int month, int year, Compensation type, Status status) {
        return timeoffRequestRepo.findAllUnpaidRequest(month, year, type, status);
    }

    @Override
    public List<TimeOffRequestEntity> findAllAnnualRequests(int month, int year, Compensation compensation, Status status, AccrualType accrualType) {
        return timeoffRequestRepo.findAllAnnualRequests(month, year, compensation, status, accrualType);
    }

    @Override
    public List<TimeOffRequestEntity> findFixedRequests(int month, int year, Status status, AccrualType accrualType) {
        return timeoffRequestRepo.findFixedRequests(month, year, status, accrualType);
    }

    @Override
    public void saveAllLeaveBalance(List<LeaveBalanceEntity> entities) {
        leaveBalanceRepository.saveAll(entities);
    }

    @Override
    public boolean existsOverlappingRequest(String userId, String policyId, LocalDate startDate, LocalDate endDate) {
        return timeoffRequestRepo.existsOverlappingRequest(userId,policyId,startDate,endDate);
    }
}
