package com.uniq.tms.tms_microservice.modules.payrollManagement.adapter;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollSettingEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollAmountEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.PayRollProjection;
import java.util.List;
import java.util.Optional;

public interface PayRollAdapter {

    PayRollSettingEntity save(PayRollSettingEntity entity);
    Optional<PayRollSettingEntity> findFirst();
    PayRollEntity savePayroll(PayRollEntity entity);
    UserPayRollEntity saveUserPayroll(UserPayRollEntity mapping);
    List<UserPayRollEntity> findExistingUserPayrolls(List<String> userIds);
    void deleteAll(List<UserPayRollEntity> userpayRolls);
    List<UserPayRollEntity> saveAllUserPayroll(List<UserPayRollEntity> userPayRoll);
    PayRollEntity savePayRoll(PayRollEntity payRollEntity);
    List<UserPayRollEntity> getAllUserPayroll();
    void saveAllUserPayrollAmount(List<UserPayRollAmountEntity> userPayRollAmountEntityList);
    List<UserPayRollAmountEntity> getPayrollAmount(String id, String month);
    List<UserPayRollAmountEntity> getAllByMonthAndYear(String month);
    List<PayRollProjection> getAllPayrollNameAndId();
    Optional<UserPayRollAmountEntity> findUserPayrollAmountByUserId(String userId);
    UserPayRollAmountEntity saveUserPayRollAmount(UserPayRollAmountEntity entity);
    PayRollEntity getPayRoll(String payRollId);
    Optional<PayRollEntity> findById(String id);
    List<PayRollEntity> findAll();
    PayRollEntity save(PayRollEntity entity);
    void deleteUserPayrollById(String payrollId);

}
