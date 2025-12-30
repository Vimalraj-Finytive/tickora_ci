package com.uniq.tms.tms_microservice.modules.payrollManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.payrollManagement.adapter.PayRollAdapter;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollSettingEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollAmountEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.UserPayRollAmountModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.PayRollProjection;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.UserPayRollAmount;
import com.uniq.tms.tms_microservice.modules.payrollManagement.repository.PayRollRepository;
import com.uniq.tms.tms_microservice.modules.payrollManagement.repository.PayRollSettingRepository;
import com.uniq.tms.tms_microservice.modules.payrollManagement.repository.UserPayRollAmountRepository;
import com.uniq.tms.tms_microservice.modules.payrollManagement.repository.UserPayRollRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PayRollAdapterImpl implements PayRollAdapter {

    private final PayRollSettingRepository repository;
    private final PayRollRepository payrollRepo;
    private final UserPayRollRepository userPayrollRepo;
    private final UserPayRollAmountRepository userPayrollAmountRepository;


    public PayRollAdapterImpl(PayRollSettingRepository repository, PayRollRepository payrollRepo,
                              UserPayRollRepository userPayrollRepo, UserPayRollAmountRepository userPayrollAmountRepository) {
        this.repository = repository;
        this.payrollRepo = payrollRepo;
        this.userPayrollRepo = userPayrollRepo;
        this.userPayrollAmountRepository = userPayrollAmountRepository;
    }

    public PayRollSettingEntity save(PayRollSettingEntity entity) {
        return repository.save(entity);
    }

    public Optional<PayRollSettingEntity> findFirst() {
        return repository.findFirstBy();
    }

    @Override
    public PayRollEntity savePayroll(PayRollEntity entity) {
        return payrollRepo.save(entity);
    }

    public PayRollEntity save(PayRollEntity entity) {
        return payrollRepo.save(entity);
    }

    public UserPayRollEntity saveUserPayroll(UserPayRollEntity mapping) {
        return userPayrollRepo.save(mapping);
    }

    @Override
    public List<UserPayRollEntity> findExistingUserPayrolls(List<String> userIds) {
        return userPayrollRepo.findExistingUserPayrolls(userIds);
    }

    @Override
    public void deleteAll(List<UserPayRollEntity> userpayRolls) {
        userPayrollRepo.deleteAll(userpayRolls);
    }

    @Override
    public List<UserPayRollEntity> saveAllUserPayroll(List<UserPayRollEntity> userPayRoll) {
        return userPayrollRepo.saveAll(userPayRoll);
    }

    @Override
    public PayRollEntity savePayRoll(PayRollEntity payRollEntity) {
        return payrollRepo.save(payRollEntity);
    }

    @Override
    public List<UserPayRollEntity> getAllUserPayroll(List<String> userIds) {
        return userPayrollRepo.findAllByActiveUsers(userIds);
    }

    @Override
    public void saveAllUserPayrollAmount(List<UserPayRollAmountEntity> userPayRollAmountEntityList) {
        userPayrollAmountRepository.saveAll(userPayRollAmountEntityList);
    }

    @Override
    public List<UserPayRollAmountEntity> getPayrollAmount(String id, String month) {
        return userPayrollAmountRepository.findAllByPayrollIdAndMonth(id, month);
    }

    @Override
    public List<UserPayRollAmountEntity> getAllByMonthAndYear(String month) {
        return userPayrollAmountRepository.findAllByMonth(month);
    }

    @Override
    public List<PayRollProjection> getAllPayrollNameAndId() {
        return payrollRepo.findAllIdAndName();
    }

    @Override
    public Optional<UserPayRollAmountEntity> findUserPayrollAmountByUserIdAndMonth(String userId, String month) {
        return userPayrollAmountRepository.findByUser_UserIdAndMonth(userId, month);
    }

    @Override
    public UserPayRollAmountEntity saveUserPayRollAmount(UserPayRollAmountEntity entity) {
        return userPayrollAmountRepository.save(entity);
    }


    @Override
    public PayRollEntity getPayRoll(String payRollId) {
        return payrollRepo.findById(payRollId).get();
    }

    @Override
    public Optional<PayRollEntity> findById(String id) {
        return payrollRepo.findActiveById(id);
    }

    @Override
    public List<PayRollEntity> findAll() {
        return payrollRepo.findByIsActiveTrue();
    }

    @Override
    public void deleteUserPayrollById(String payrollId) {
        userPayrollRepo.deleteByPayrollId(payrollId);
    }

    public List<UserPayRollAmount> findAllByMonth(String month) {
        return userPayrollRepo.findAllByMonth(month);
    }

    @Override
    public Optional<UserPayRollAmountEntity> getUserPayrollAmount(String userId, String month) {
        return userPayrollAmountRepository.getUserPayrollAmount(userId, month);
    }

    @Override
    public List<UserEntity> findUsersByPayrollId(String payrollId, LocalDate date) {
        return userPayrollRepo.findUsersByPayrollId(payrollId, date);
    }

    @Override
    public List<String> findAllUsersByMonth(LocalDate date) {
        return userPayrollRepo.findAllUsersByMonth(date);
    }
}
