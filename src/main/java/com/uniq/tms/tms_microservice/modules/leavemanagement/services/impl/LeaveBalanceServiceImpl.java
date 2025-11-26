package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;
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
       private final TimeOffPolicyEntityMapper timeOffPolicyEntityMapper;

    public LeaveBalanceServiceImpl(LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper) {
        this.leaveBalanceAdapter = leaveBalanceAdapter;
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
        for (LeaveBalanceEntity current : currentBalances) {
            log.info("loop started");
            LeaveBalanceEntity next = new LeaveBalanceEntity();
            next.setPolicy(current.getPolicy());
            next.setUserId(current.getUser().getUserId());
            log.info("type");
            if (type == AccrualType.MONTHLY) {
                log.info("month");
                next.setPeriodStartDate(current.getPeriodStartDate().plusMonths(1));
                next.setPeriodEnd(current.getPeriodEnd().plusMonths(1));
                next.setNextAccrualDate(LocalDate.now(zoneId).plusMonths(1));
                log.info("month saved");
            }
            else {
                next.setPeriodStartDate(current.getPeriodStartDate().plusYears(1));
                next.setPeriodEnd(current.getPeriodEnd().plusYears(1));
                next.setNextAccrualDate(LocalDate.now(zoneId).plusYears(1));
            }
            log.info("before carryForward{}",current.getPolicy().getCarryForward());
            double carryForward = current.getPolicy().getCarryForward() ? current.getCarryForwardUnits() : 0.0;
            log.info("get carryforward{}:",carryForward);
            next.setCarryForwardUnits((double) current.getPolicy().getMaxCarryForwardUnits());
            next.setTotalUnits(current.getTotalUnits());
            next.setExpiredUnits(0.0);
            next.setLeaveTakenUnits(0.0);
            next.setBalanceUnits(next.getTotalUnits() + carryForward);
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
