package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.*;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

public interface TimeoffPolicyAdapter {

    TimeoffPolicyEntity findPolicyById(String policyId);
    LeaveBalanceEntity findLeaveBalance(String payrollId, String userId);
    boolean existsTimeoffRequest(String userId, String policyId);
    TimeoffRequestEntity saveRequest(TimeoffRequestEntity entity);
    List<UsersRequestMappingEntity> saveUsersRequestMapping(List<UsersRequestMappingEntity> entity);
    TimeoffRequestEntity findByUserIdAndRequestDate(String userId, LocalDate requestDate);
    List<TimeoffRequestEntity> saveAllRequest(List<TimeoffRequestEntity> entities);
    List<TimeoffRequestEntity> findStartByDate(LocalDate startDate);
    List<LeaveBalanceEntity> saveAllLeaveBalance(List<LeaveBalanceEntity> entities);
    TimeoffPolicyEntity savePolicy(TimeoffPolicyEntity policy);
    TimeoffPolicyEntity findByPolicyId(String policyId);
    List<TimeoffPolicyEntity> findPoliciesByIds(List<String> policyIds);
    List<TimeoffPolicyEntity> findAll();
    TimeoffPolicyEntity findById(String id);
    List<String> findUserIdsByPolicyId(String policyId);
    String findUsernameByUserId(String userId);
    List<TimeoffPolicyEntity> findByUserId(String userId);
}
