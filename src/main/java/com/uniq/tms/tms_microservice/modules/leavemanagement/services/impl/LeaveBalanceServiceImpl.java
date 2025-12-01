package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffRequestAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.UserPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyKey;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyProjection;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.LeaveBalanceService;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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
       private final TimeOffRequestAdapter timeOffRequestAdapter;
       private final UserPolicyAdapter userPolicyAdapter;

    public LeaveBalanceServiceImpl(LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyAdapter timeOffPolicyAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper, TimeOffRequestAdapter timeOffRequestAdapter, UserPolicyAdapter userPolicyAdapter) {
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
        this.timeOffRequestAdapter = timeOffRequestAdapter;
        this.userPolicyAdapter = userPolicyAdapter;
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
            } else if (type == AccrualType.ANNUALLY){
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
        List<MonthlySummaryEntity> summaryEntityList = new ArrayList<>();
        log.info(" fetch userPolicy list");
        List<String> userIds = userPolicyAdapter.findAllUserIdsInUserPolicies();

        log.info("fetch monthly leaveBalance");
        List<LeaveBalanceEntity> list =
                leaveBalanceAdapter.findBalancesByMonthYearAndAccrualType(month, year, AccrualType.MONTHLY);

        Map<String, List<LeaveBalanceEntity>> monthlyLeaveBalance =
                list.stream()
                        .collect(Collectors.groupingBy(lb -> lb.getUser().getUserId()));

        log.info("fetch unpaid requests");
        List<TimeOffRequestEntity> unpaidRequests = timeOffRequestAdapter.findAllUnpaidRequest( month, year, Compensation.UNPAID, Status.APPROVED);
        Map<String, List<TimeOffRequestEntity>> unpaidMap =
                unpaidRequests.stream()
                        .collect(Collectors.groupingBy(r -> r.getUser().getUserId()));

        log.info("fetch annual requests");
        List<TimeOffRequestEntity> annualRequests = timeOffRequestAdapter.findAllAnnualRequests(month, year, Compensation.PAID, Status.APPROVED, AccrualType.ANNUALLY);
        Map<String, List<TimeOffRequestEntity>> annualRequestsMap =
                annualRequests.stream()
                        .collect(Collectors.groupingBy(r -> r.getUser().getUserId()));

        log.info("fetch annual leaveBalance");
        List<LeaveBalanceEntity> leaveBalanceEntities = leaveBalanceAdapter.findAnnualLeaveBalances(year, AccrualType.ANNUALLY);
        Map<String, List<LeaveBalanceEntity>> leaveBalanceMap =
                leaveBalanceEntities.stream()
                        .collect(Collectors.groupingBy(lb -> lb.getUser().getUserId()));

        log.info("fetch fixed requests");
        List<TimeOffRequestEntity> fixedRequests = timeOffRequestAdapter.findFixedRequests(month, year, Status.APPROVED, AccrualType.FIXED);
        Map<String, List<TimeOffRequestEntity>> fixedRequestsMap =
                fixedRequests.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.getUser().getUserId()));

        log.info("fetch fixed leaveBalance");
        List<LeaveBalanceEntity> fixedLeaveBalance = leaveBalanceAdapter.findAllFixedAccrual(month, year, AccrualType.FIXED);
        Map<String, Double> balanceMap = fixedLeaveBalance.stream()
                .collect(Collectors.toMap(
                        lb -> lb.getUser().getUserId(),
                        lb -> lb.getBalanceUnits() != null ? lb.getBalanceUnits() : 0.0,
                        (existing, replacement) -> existing
                ));

        for (String userId : userIds){
            log.info("loop starts");
            MonthlySummaryEntity summaryEntity = new MonthlySummaryEntity();
            int totalLeavesTaken = 0;
            int paidLeavesTaken = 0;
            int unpaidLeavesTaken = 0;
            int totalUnitsAvailable = 0;
            int balanceUnits = 0;
            int halfDayUnits = 0;
            int fullDayUnits = 0;
            int hoursUnits = 0;
            for (LeaveBalanceEntity entity : monthlyLeaveBalance.getOrDefault(userId, Collections.emptyList())){
                if (entity.getPolicy().getEntitledType() == EntitledType.DAY){
                    fullDayUnits += entity.getLeaveTakenUnits();
                    paidLeavesTaken += entity.getLeaveTakenUnits();
                    totalUnitsAvailable += entity.getTotalUnits();
                    balanceUnits += entity.getBalanceUnits();
                }
                else if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY){
                    halfDayUnits += (int)(entity.getLeaveTakenUnits()*2);
                    paidLeavesTaken += (int)(entity.getLeaveTakenUnits()*2);
                    totalUnitsAvailable += (int)(entity.getTotalUnits()*2);
                    balanceUnits += entity.getBalanceUnits();
                }
                else {
                    hoursUnits += entity.getLeaveTakenUnits();
                    paidLeavesTaken += entity.getLeaveTakenUnits();
                    totalUnitsAvailable += entity.getTotalUnits();
                    balanceUnits += entity.getBalanceUnits();
                }
            }
            for (TimeOffRequestEntity request : unpaidMap.getOrDefault(userId, Collections.emptyList())){
                unpaidLeavesTaken += request.getUnitsRequested();
                fullDayUnits += request.getUnitsRequested();
            }
            for (TimeOffRequestEntity entity : annualRequestsMap.getOrDefault(userId, Collections.emptyList())){
                if (entity.getPolicy().getEntitledType() == EntitledType.DAY){
                    fullDayUnits += entity.getUnitsRequested();
                    paidLeavesTaken += entity.getUnitsRequested();
                }
                else if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY){
                    halfDayUnits += (2*entity.getUnitsRequested());
                    paidLeavesTaken +=(2*entity.getUnitsRequested());
                }
                else {
                    hoursUnits += entity.getUnitsRequested();
                    paidLeavesTaken += entity.getUnitsRequested();
                }
            }
            for (LeaveBalanceEntity leaveBalance : leaveBalanceMap.getOrDefault(userId, Collections.emptyList())){
                totalUnitsAvailable += leaveBalance.getTotalUnits();
                balanceUnits += leaveBalance.getBalanceUnits();
            }
            for (TimeOffRequestEntity request : fixedRequestsMap.getOrDefault(userId, Collections.emptyList())){
                LocalDate monthStart = LocalDate.of(year, month, 1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
                LocalDate effectiveStart = request.getStartDate().isBefore(monthStart) ? monthStart : request.getStartDate();
                LocalDate effectiveEnd = request.getEndDate().isAfter(monthEnd) ? monthEnd : request.getEndDate();
                int days = (int)ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;
                fullDayUnits += days;
                paidLeavesTaken += fullDayUnits;
                totalUnitsAvailable += request.getPolicy().getEntitledUnits();
                balanceUnits += balanceMap.getOrDefault(userId, 0.0);
                if (effectiveStart.equals(request.getStartDate()) && !effectiveEnd.equals(request.getEndDate())){
                    balanceUnits = balanceUnits - request.getEndDate().getDayOfMonth();
                }
            }
            totalLeavesTaken = paidLeavesTaken + unpaidLeavesTaken;
            summaryEntity.setUserId(userId);
            summaryEntity.setYear(year);
            summaryEntity.setMonth(month);
            summaryEntity.setTotalLeavesTaken(totalLeavesTaken);
            summaryEntity.setPaidLeavesTaken(paidLeavesTaken);
            summaryEntity.setUnpaidLeavesTaken(unpaidLeavesTaken);
            summaryEntity.setFullDayUnits(fullDayUnits);
            summaryEntity.setHalfDayUnits(halfDayUnits);
            summaryEntity.setTotalUnitsAvailable(totalUnitsAvailable);
            summaryEntity.setBalanceUnits(balanceUnits);
            summaryEntity.setHoursUnits(hoursUnits);
            log.info("added summary");
            summaryEntityList.add(summaryEntity);
        }
        leaveBalanceAdapter.saveAllSummary(summaryEntityList);
        log.info("saved all summary");
    }

}
