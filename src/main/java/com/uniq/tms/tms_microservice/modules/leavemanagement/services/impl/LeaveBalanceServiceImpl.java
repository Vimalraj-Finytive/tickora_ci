package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyKey;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ResetFrequency;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyProjection;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.LeaveBalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

       private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");
       private static final Logger log = LoggerFactory.getLogger(TimeOffPolicyServiceImpl.class);

       private final LeaveBalanceAdapter leaveBalanceAdapter;
       private final TimeOffPolicyAdapter timeOffPolicyAdapter;
       private final TimeOffPolicyEntityMapper timeOffPolicyEntityMapper;

    public LeaveBalanceServiceImpl(LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyAdapter timeOffPolicyAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper) {
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
    }

    @Override
    public List<LeaveBalanceModel> getLeaveBalance(String userId) {
        List<LeaveBalanceEntity> entities = leaveBalanceAdapter.findBalance(userId);
        return timeOffPolicyEntityMapper.toBalanceModelList(entities);
        }


    @Override
    public void updateMonthlyLeaveBalance() {
        log.info("update monthly");
        LocalDate now = LocalDate.now(zoneId);
        int year = now.getYear();
        int month = now.getMonthValue()-1;
        if (month ==0){
            month =12;
            year -= 1;
        }
        List<LeaveBalanceEntity> currentBalances =
                leaveBalanceAdapter.findBalancesByMonthYearAndAccrualType(month, year, AccrualType.MONTHLY);
        log.info("fetched leave balance");
        if (currentBalances.isEmpty()) {
            return;
        }
        log.info("leave balance not empty");
        updateLeaveBalance(currentBalances, AccrualType.MONTHLY);
    }

    private void updateLeaveBalance(List<LeaveBalanceEntity> currentBalances, AccrualType type){

        List<LeaveBalanceEntity> nextLeaveBalance = new ArrayList<>();
        List<UserPolicyProjection> result = timeOffPolicyAdapter.findUserPolicyValidTo();
        Map<UserPolicyKey, LocalDate> validToMap = result.stream()
                .collect(Collectors.toMap(
                        UserPolicyProjection::key,
                        UserPolicyProjection::validTo
                ));
        for (LeaveBalanceEntity current : currentBalances) {
            log.info("loop started");
            String userId = current.getUser().getUserId();
            String policyId = current.getPolicy().getPolicyId();
            UserPolicyKey key = new UserPolicyKey(userId, policyId);
            LocalDate validTo = validToMap.get(key);
            if ( validTo != null && validTo.isBefore(LocalDate.now(zoneId))){
                continue;
            }
            LeaveBalanceEntity next = new LeaveBalanceEntity();
            next.setPolicy(current.getPolicy());
            next.setUser(current.getUser());
            log.info("type");
            double carry =  current.getPolicy().getMaxCarryForwardUnits();
            double expiredUnits = 0.0;
            double totalUnits = 0.0;
            if (type == AccrualType.MONTHLY) {
                if (current.getPolicy().getResetFrequency() == ResetFrequency.ANNUALLY) {
                    boolean isResetDay = LocalDate.now().getDayOfYear() == 1;
                    if (isResetDay) {
                        if (current.getPolicy().getCarryForward()) {
                            if (carry < current.getPolicy().getEntitledUnits()) {
                                expiredUnits = current.getPolicy().getEntitledUnits() - carry;
                            } else {
                                carry = current.getBalanceUnits();
                            }
                            totalUnits = current.getBalanceUnits() + carry;
                        } else {
                            expiredUnits = current.getBalanceUnits();
                            carry = 0.0;
                            totalUnits = current.getPolicy().getEntitledUnits();
                        }
                    } else {
                        carry = 0.0;
                        totalUnits = current.getBalanceUnits() + current.getPolicy().getEntitledUnits();
                    }
                }
                else if (current.getPolicy().getResetFrequency() == ResetFrequency.MONTHLY) {
                    if (current.getPolicy().getCarryForward()) {
                        if (carry < current.getBalanceUnits()) {
                            expiredUnits = current.getBalanceUnits() - carry;
                        } else {
                            carry = current.getBalanceUnits();
                        }
                        totalUnits = carry + current.getPolicy().getEntitledUnits();
                    }else {
                        carry = 0.0;
                        totalUnits = current.getPolicy().getEntitledUnits();
                    }
                }
                log.info("month");
                next.setPeriodStartDate(LocalDate.now(zoneId));
                LocalDate periodEnd = current.getPeriodEnd().plusMonths(1);
                LocalDate nextAccrual = LocalDate.now(zoneId).plusMonths(1);
                if (periodEnd.isAfter(validTo)){
                    periodEnd = validTo;
                    nextAccrual = null;
                }
                next.setPeriodEnd(periodEnd);
                next.setNextAccrualDate(nextAccrual);
                log.info("month saved");
            } else {
                if (current.getPolicy().getCarryForward()) {
                    if (carry < current.getBalanceUnits()){
                        expiredUnits = current.getBalanceUnits() - carry;
                    }
                    else {
                        carry = current.getBalanceUnits();
                    }
                    totalUnits = carry + current.getPolicy().getEntitledUnits();
                }
                else {
                    expiredUnits = current.getBalanceUnits();
                    carry = 0.0;
                    totalUnits = current.getPolicy().getEntitledUnits();
                }
                next.setPeriodStartDate(current.getPeriodStartDate().plusYears(1));
                next.setPeriodEnd(current.getPeriodEnd().plusYears(1));
                next.setNextAccrualDate(LocalDate.now(zoneId).plusYears(1));
            }
            log.info("before carryForward{}",current.getPolicy().getCarryForward());
            next.setCarryForwardUnits(carry);
            next.setTotalUnits(totalUnits);
            next.setExpiredUnits(expiredUnits);
            next.setLeaveTakenUnits(0.0);
            next.setBalanceUnits(totalUnits);
            next.setLastAccrualDate(LocalDate.now());
            log.info("before add");
            nextLeaveBalance.add(next);
            log.info("added");
        }
        leaveBalanceAdapter.saveLeaveBalances(nextLeaveBalance);
        log.info("saved");
    }

    @Override
    public void updateYearlyLeaveBalance() {
        LocalDate now = LocalDate.now(zoneId);
        int year = now.getYear()-1;
        List<LeaveBalanceEntity> currentBalances =
                leaveBalanceAdapter.findBalancesByYearAndAccrualType(year, AccrualType.ANNUALLY);
        if (currentBalances.isEmpty()) {
            return;
        }
        updateLeaveBalance(currentBalances, AccrualType.ANNUALLY);
    }

    @Override
    public void updateLeaveSummary() {

        LocalDate now = LocalDate.now(zoneId);
        int year = now.getYear();
        int month = now.getMonthValue()-1;
        if (month ==0){
            month =12;
            year -= 1;
        }
        List<LeaveBalanceEntity> list =
                leaveBalanceAdapter.findBalancesByMonthYearAndAccrualType(month, year, AccrualType.MONTHLY);

        Map<String, LeaveBalanceEntity> map = list.stream()
                .collect(Collectors.toMap(
                        lb -> lb.getUser().getUserId(),
                        lb -> lb,
                        (lb1, lb2) -> lb1
                ));
    }
}
