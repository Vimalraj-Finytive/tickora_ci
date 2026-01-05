package com.uniq.tms.tms_microservice.modules.payrollManagement.adapter;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollSettingEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollAmountEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.UserPayRollAmountModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.PayRollProjection;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.UserPayRollAmount;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;

import java.time.LocalDate;
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
    List<UserPayRollEntity> getAllUserPayroll(List<String> userIds);
    void saveAllUserPayrollAmount(List<UserPayRollAmountEntity> userPayRollAmountEntityList);
    List<UserPayRollAmountEntity> getPayrollAmount(String id, String month);
    List<UserPayRollAmountEntity> getAllByMonthAndYear(String month);
    List<PayRollProjection> getAllPayrollNameAndId();
    Optional<UserPayRollAmountEntity> findUserPayrollAmountByUserIdAndMonth(String userId,String month);
    UserPayRollAmountEntity saveUserPayRollAmount(UserPayRollAmountEntity entity);
    PayRollEntity getPayRoll(String payRollId);
    Optional<PayRollEntity> findById(String id);
    List<PayRollEntity> findAll();
    PayRollEntity save(PayRollEntity entity);
    void deleteUserPayrollById(String payrollId);
    List<UserPayRollAmount> findAllByMonth(String month);
    Optional<UserPayRollAmountEntity> getUserPayrollAmount(String userId, String month);
    List<UserEntity> findUsersByPayrollId(String payrollId, LocalDate date);
    List<String> findAllUsersByMonth(LocalDate date);
}
