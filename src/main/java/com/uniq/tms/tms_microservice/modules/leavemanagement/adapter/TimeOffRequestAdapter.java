package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel;
import java.time.LocalDate;
import java.util.List;

public interface TimeOffRequestAdapter {

    LeaveBalanceEntity findLeaveBalance(String payrollId, String userId);
    void saveAllLeaveBalance(List<LeaveBalanceEntity> entities);
    TimeOffRequestEntity saveRequest(TimeOffRequestEntity entity);
    List<UsersRequestMappingEntity> saveUsersRequestMapping(List<UsersRequestMappingEntity> entity);
    List<TimeOffRequestEntity> saveAllRequest(List<TimeOffRequestEntity> entities);
    List<TimeOffRequestEntity> findStartByDate(LocalDate startDate);
    List<TimeOffRequestUserModel> filterWithUser(LocalDate fromDate, LocalDate toDate, String userId);
     List<TimeOffRequestUserModel> filterCreatedByUser(LocalDate from, LocalDate to, String userId);
    List<TimeOffRequestUserModel> filterWithUserAndRole(LocalDate from, LocalDate to, int minRoleLevel);
    boolean existsTimeoffRequest(String userId, String policyId, LocalDate requestDate);
    TimeOffRequestEntity getTimeoffRequest(String policyId, String userId, LocalDate requestDate);
    List<TimeOffRequestEntity> findByStartDate(LocalDate startDate);
    boolean existsOverlappingRequest(String userId, String policyId, LocalDate startDate, LocalDate endDate);
    List<TimeOffRequestEntity> findAllUnpaidRequest(int month, int year, Compensation type, Status status);
    List<TimeOffRequestEntity> findAllAnnualRequests(int month, int year, Compensation compensation, Status status, AccrualType accrualType);
    List<TimeOffRequestEntity> findFixedRequests(int month, int year, Status status, AccrualType accrualType);


}
