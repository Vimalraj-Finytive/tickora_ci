package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.LeaveBalanceService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.TimesheetStatusEnum;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserHolidayProjection;
import com.uniq.tms.tms_microservice.shared.helper.TimesheetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

       private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");
       private static final Logger log = LoggerFactory.getLogger(LeaveBalanceServiceImpl.class);

       private final LeaveBalanceAdapter leaveBalanceAdapter;
       private final TimeOffPolicyEntityMapper timeOffPolicyEntityMapper;
       private final TimeOffRequestAdapter timeOffRequestAdapter;
       private final UserPolicyAdapter userPolicyAdapter;
       private final TimesheetAdapter timesheetAdapter;
       private final TimesheetHelper timesheetHelper;
       private final UserAdapter userAdapter;

    public LeaveBalanceServiceImpl(LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper, TimeOffRequestAdapter timeOffRequestAdapter, UserPolicyAdapter userPolicyAdapter,
                                   TimesheetAdapter timesheetAdapter, TimesheetHelper timesheetHelper, UserAdapter userAdapter) {
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
        this.timeOffRequestAdapter = timeOffRequestAdapter;
        this.userPolicyAdapter = userPolicyAdapter;
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetHelper = timesheetHelper;
        this.userAdapter = userAdapter;
    }

    @Override
    public List<LeaveBalanceModel> getLeaveBalance(String userId,String year) {
        List<LeaveBalanceEntity> entities = leaveBalanceAdapter.findBalance(userId,year);
        return timeOffPolicyEntityMapper.toBalanceModelList(entities);
    }

    @Override
    public void updateMonthlyLeaveBalance() {
        LocalDate now = LocalDate.now(zoneId);
        LocalDate previousMonth = now.minusMonths(1);
        LocalDate monthStartDate = previousMonth.withDayOfMonth(1);
        LocalDate monthEndDate = now.minusDays(1);

        List<String> usersId = userAdapter.getAllActiveUsers();
        List<String> userIds = userPolicyAdapter.findAllUserIdsInUserPolicies(now, usersId);
        List<LeaveBalanceEntity> monthlyBalances = leaveBalanceAdapter.findActiveMonthlyBalances(monthStartDate, monthEndDate, ResetFrequency.MONTHLY, AccrualType.MONTHLY, userIds);
        updateLeaveBalance(monthlyBalances, AccrualType.MONTHLY);
        List<LeaveBalanceEntity> annualBalances = leaveBalanceAdapter.findActiveMonthlyBalances(monthStartDate, monthEndDate, ResetFrequency.ANNUALLY, AccrualType.MONTHLY, userIds);
        updateLeaveBalance(annualBalances, AccrualType.MONTHLY);
    }

    private void updateLeaveBalance(List<LeaveBalanceEntity> currentBalances, AccrualType type){

        List<LeaveBalanceEntity> nextLeaveBalance = new ArrayList<>();
        LocalDate today = LocalDate.now(zoneId);
        for (LeaveBalanceEntity current : currentBalances) {
            UserEntity user = current.getUser();
            TimeOffPolicyEntity policy = current.getPolicy();
            LocalDate validTo = policy.getValidityEndDate();
            if ( validTo != null && validTo.isBefore(LocalDate.now(zoneId))){
                continue;
            }
            LeaveBalanceEntity next = new LeaveBalanceEntity();
            next.setPolicy(policy);
            next.setUser(user);
            double balance = current.getBalanceUnits();
            double entitled = policy.getEntitledUnits();
            double maxCarry =  policy.getMaxCarryForwardUnits()== null? 0.0:current.getPolicy().getMaxCarryForwardUnits();
            double[] result = calculateCarry(
                    balance,
                    entitled,
                    maxCarry,
                    policy.getCarryForward(),
                    policy.getResetFrequency(),
                    policy.getAccrualType(),
                    policy.getEntitledType(),
                    today);
            log.info("monthly");
            if (type == AccrualType.MONTHLY) {
                next.setPeriodStartDate(today);
                LocalDate periodEnd = current.getPeriodEnd().plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                LocalDate nextAccrual = today.plusMonths(1);
                if (validTo!= null && periodEnd.isAfter(validTo)){
                    periodEnd = validTo;
                    nextAccrual = null;
                }
                next.setPeriodEnd(periodEnd);
                next.setNextAccrualDate(nextAccrual);
            } else if (type == AccrualType.ANNUALLY){
                next.setPeriodStartDate(today);
                next.setPeriodEnd(current.getPeriodEnd().plusYears(1));
                next.setNextAccrualDate(LocalDate.now(zoneId).plusYears(1));
            }
            next.setCarryForwardUnits(result[0]);
            next.setExpiredUnits(result[1]);
            next.setTotalUnits(result[2]);
            next.setLeaveTakenUnits(0.0);
            next.setBalanceUnits(result[2]);
            next.setLastAccrualDate(today);
            next.setActive(true);
            nextLeaveBalance.add(next);
        }
        leaveBalanceAdapter.saveLeaveBalances(nextLeaveBalance);
        log.info("saved");
    }

    private double[] calculateCarry(
            double balance,
            double entitled,
            double maxCarry,
            boolean carryForward,
            ResetFrequency resetFrequency,
            AccrualType accrualType,
            EntitledType entitledType,
            LocalDate today
    ) {
        double expired;
        double total;
        if (entitledType == EntitledType.HALF_DAY){
            entitled = entitled*0.5;
        }
        if (resetFrequency == ResetFrequency.ANNUALLY && accrualType == AccrualType.MONTHLY &&
                today.getDayOfYear() != 1) {
            total = balance + entitled;
            return new double[]{0.0, 0.0, total};
        }

        if (!carryForward) {
            expired = balance;
            total = entitled;
            return new double[]{0.0, expired, total};
        }

        double carry = Math.min(balance, maxCarry);
        expired = balance - carry;
        total = carry + entitled;
        return new double[]{carry, expired, total};
    }

    @Override
    public void updateYearlyLeaveBalance() {
        LocalDate now = LocalDate.now(zoneId);
        LocalDate previousYear = now.minusYears(1);
        LocalDate monthStartDate = previousYear.withDayOfYear(1);
        LocalDate monthEndDate = now.minusDays(1);

        List<String> usersId = userAdapter.getAllActiveUsers();
        List<String> userIds = userPolicyAdapter.findAllUserIdsInUserPolicies(now, usersId);
        List<LeaveBalanceEntity> currentBalances = leaveBalanceAdapter.findActiveMonthlyBalances(monthStartDate, monthEndDate, ResetFrequency.ANNUALLY, AccrualType.ANNUALLY, userIds);
        updateLeaveBalance(currentBalances, AccrualType.ANNUALLY);
    }


//    @Override
//    public void updateMonthlyLeaveBalance() {
//        log.info("update monthly");
//        LocalDate now = LocalDate.now(zoneId);
//        int year = now.getYear();
//        int month = now.getMonthValue()-1;
//        if (month ==0){
//            month =12;
//            year -= 1;
//        }
//        List<LeaveBalanceEntity> currentBalances =
//                leaveBalanceAdapter.findBalancesByMonthYearAndAccrualType(month, year, AccrualType.MONTHLY);
//        log.info("fetched leave balance");
//        if (currentBalances.isEmpty()) {
//            return;
//        }
//        log.info("leave balance not empty");
//        List<UserPolicyProjection> result = userPolicyAdapter.findUserPolicyValidTo(AccrualType.MONTHLY);
//        updateLeaveBalance(currentBalances, AccrualType.MONTHLY, result);
//    }
//
//    private void updateLeaveBalance(List<LeaveBalanceEntity> currentBalances, AccrualType type, List<UserPolicyProjection> result){
//
//        List<LeaveBalanceEntity> nextLeaveBalance = new ArrayList<>();
//        Map<UserPolicyKey, LocalDate> validToMap = result.stream()
//                .filter(p -> p.validTo() != null)
//                .collect(Collectors.toMap(
//                        UserPolicyProjection::key,
//                        UserPolicyProjection::validTo
//                ));
//        for (LeaveBalanceEntity current : currentBalances) {
//            log.info("loop started");
//            String userId = current.getUser().getUserId();
//            String policyId = current.getPolicy().getPolicyId();
//            UserPolicyKey key = new UserPolicyKey(userId, policyId);
//            LocalDate validTo = validToMap.get(key);
//            if ( validTo != null && validTo.isBefore(LocalDate.now(zoneId))){
//                continue;
//            }
//            LeaveBalanceEntity next = new LeaveBalanceEntity();
//            next.setPolicy(current.getPolicy());
//            next.setUser(current.getUser());
//            log.info("type");
//            double carry =  current.getPolicy().getMaxCarryForwardUnits()== null? 0.0:current.getPolicy().getMaxCarryForwardUnits() ;
//            double expiredUnits = 0.0;
//            double totalUnits = 0.0;
//            if (type == AccrualType.MONTHLY) {
//                log.info("monthly");
//                if (current.getPolicy().getResetFrequency() == ResetFrequency.ANNUALLY) {
//                    boolean isResetDay = LocalDate.now().getDayOfYear() == 1;
//                    if (isResetDay) {
//                        if (current.getPolicy().getCarryForward()) {
//                            if (carry < current.getPolicy().getEntitledUnits()) {
//                                expiredUnits = current.getPolicy().getEntitledUnits() - carry;
//                            } else {
//                                carry = current.getBalanceUnits();
//                            }
//                            totalUnits = current.getBalanceUnits() + carry;
//                        } else {
//                            expiredUnits = current.getBalanceUnits();
//                            carry = 0.0;
//                            totalUnits = current.getPolicy().getEntitledUnits();
//                        }
//                    } else {
//                        carry = 0.0;
//                        totalUnits = current.getBalanceUnits() + current.getPolicy().getEntitledUnits();
//                    }
//                }
//                else if (current.getPolicy().getResetFrequency() == ResetFrequency.MONTHLY) {
//                    if (current.getPolicy().getCarryForward()) {
//                        if (carry < current.getBalanceUnits()) {
//                            expiredUnits = current.getBalanceUnits() - carry;
//                        } else {
//                            carry = current.getBalanceUnits();
//                        }
//                        totalUnits = carry + current.getPolicy().getEntitledUnits();
//                    }else {
//                        carry = 0.0;
//                        totalUnits = current.getPolicy().getEntitledUnits();
//                    }
//                }
//                log.info("month");
//                next.setPeriodStartDate(LocalDate.now(zoneId));
//                LocalDate periodEnd = current.getPeriodEnd().plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());;
//                LocalDate nextAccrual = LocalDate.now(zoneId).plusMonths(1);
//                if (validTo!= null && periodEnd.isAfter(validTo)){
//                    periodEnd = validTo;
//                    nextAccrual = null;
//                }
//                next.setPeriodEnd(periodEnd);
//                next.setNextAccrualDate(nextAccrual);
//                log.info("month saved");
//            } else if (type == AccrualType.ANNUALLY){
//                if (current.getPolicy().getCarryForward()) {
//                    if (carry < current.getBalanceUnits()){
//                        expiredUnits = current.getBalanceUnits() - carry;
//                    }
//                    else {
//                        carry = current.getBalanceUnits();
//                    }
//                    totalUnits = carry + current.getPolicy().getEntitledUnits();
//                }
//                else {
//                    expiredUnits = current.getBalanceUnits();
//                    carry = 0.0;
//                    totalUnits = current.getPolicy().getEntitledUnits();
//                }
//                next.setPeriodStartDate(current.getPeriodStartDate().plusYears(1));
//                next.setPeriodEnd(current.getPeriodEnd().plusYears(1));
//                next.setNextAccrualDate(LocalDate.now(zoneId).plusYears(1));
//            }
//            log.info("before carryForward{}",current.getPolicy().getCarryForward());
//            next.setCarryForwardUnits(carry);
//            next.setTotalUnits(totalUnits);
//            next.setExpiredUnits(expiredUnits);
//            next.setLeaveTakenUnits(0.0);
//            next.setBalanceUnits(totalUnits);
//            next.setLastAccrualDate(LocalDate.now());
//            next.setActive(true);
//            log.info("before add");
//            nextLeaveBalance.add(next);
//            log.info("added");
//        }
//        leaveBalanceAdapter.saveLeaveBalances(nextLeaveBalance);
//        log.info("saved");
//    }
//
//    @Override
//    public void updateYearlyLeaveBalance() {
//        LocalDate now = LocalDate.now(zoneId);
//        int year = now.getYear()-1;
//        List<LeaveBalanceEntity> currentBalances =
//                leaveBalanceAdapter.findBalancesByYearAndAccrualType(year, AccrualType.ANNUALLY);
//        if (currentBalances.isEmpty()) {
//            return;
//        }
//        List<UserPolicyProjection> result = userPolicyAdapter.findUserPolicyValidTo(AccrualType.ANNUALLY);
//        updateLeaveBalance(currentBalances, AccrualType.ANNUALLY, result);
//    }

    @Override
    public void updateMonthlyLeaveSummary(String orgId) {
        LocalDate now = LocalDate.now(zoneId).withDayOfMonth(1);
        LocalDate previousMonth = now.minusMonths(1);
        int month = previousMonth.getMonthValue();
        int year = previousMonth.getYear();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        List<MonthlySummaryEntity> summaryEntityList = new ArrayList<>();
        int studentLevel = UserRole.STUDENT.getHierarchyLevel();
        int superAdminLevel = UserRole.SUPERADMIN.getHierarchyLevel();
        List<Integer> higherRoleLevels = Arrays.stream(UserRole.values())
                .map(UserRole::getHierarchyLevel)
                .filter(level -> level < studentLevel && level > superAdminLevel)
                .toList();
        List<String> usersId = userAdapter.getMembersByRole(orgId, higherRoleLevels)
                .stream().map(UserEntity::getUserId)
                .filter(Objects::nonNull)
                .toList();
        List<String> userIds = userPolicyAdapter.findAllUserIdsInUserPolicies(LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth()), usersId);
        TimesheetHelper.WorkScheduleResult result = timesheetHelper.fetchWorkSchedulesAndDays(userIds.toArray(new String[0]));
        Map<String, Set< DayOfWeek >> userWorkingDaysMap = result.getUserWorkingDaysMap();
        List<UserHolidayProjection> results =
                userAdapter.findUserHolidays(userIds);
        Map<String, Set<LocalDate>> userHolidayMap = results.stream()
                .collect(Collectors.groupingBy(
                        UserHolidayProjection::getUserId,
                        Collectors.mapping(
                                UserHolidayProjection::getDate,
                                Collectors.toSet()
                        )
                ));
        log.info("fetch monthly leaveBalance");
        List<LeaveBalanceEntity> list =
                leaveBalanceAdapter.findBalancesByMonthYearAndAccrualType(month, year, AccrualType.MONTHLY);

        Map<String, List<LeaveBalanceEntity>> monthlyLeaveBalance =
                list.stream()
                        .collect(Collectors.groupingBy(lb -> lb.getUser().getUserId()));

        log.info("fetch annual requests");
        List<TimeOffRequestEntity> annualRequests = timeOffRequestAdapter.findAllAnnualRequests(month, year, Compensation.PAID, Status.APPROVED, AccrualType.ANNUALLY);
        Map<String, List<TimeOffRequestEntity>> annualRequestsMap =
                annualRequests.stream()
                        .collect(Collectors.groupingBy(r -> r.getUser().getUserId()));

        log.info("fetch annual leaveBalance");
        List<LeaveBalanceEntity> leaveBalanceEntities = leaveBalanceAdapter.findBalancesByYearAndAccrualType(year, AccrualType.ANNUALLY);
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
            int totalLeavesTaken;
            int paidLeavesTaken = 0;
            int unpaidLeavesTaken;
            int totalUnitsAvailable = 0;
            int balanceUnits = 0;
            int halfDayUnits = 0;
            int fullDayUnits = 0;
            int hoursUnits = 0;
            int presentUnits = 0;
            for (LeaveBalanceEntity entity : monthlyLeaveBalance.getOrDefault(userId, Collections.emptyList())){
                if (entity.getPolicy().getEntitledType() == EntitledType.DAY){
                    totalUnitsAvailable += entity.getTotalUnits();
                    balanceUnits += entity.getBalanceUnits();
                }
                else if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY){
                    halfDayUnits += (int)(entity.getLeaveTakenUnits()*2);
                    paidLeavesTaken += (int)(entity.getLeaveTakenUnits()*2);
                    totalUnitsAvailable += (int)(entity.getTotalUnits()*2);
                    balanceUnits += (int)(entity.getBalanceUnits()*2);
                }
                else {
                    hoursUnits += entity.getLeaveTakenUnits();
                    paidLeavesTaken += entity.getLeaveTakenUnits();
                    totalUnitsAvailable += entity.getTotalUnits();
                    balanceUnits += entity.getBalanceUnits();
                }
            }

            for (TimeOffRequestEntity entity : annualRequestsMap.getOrDefault(userId, Collections.emptyList())){
                int units = entity.getUnitsRequested();
                EntitledType type = entity.getPolicy().getEntitledType();
                if (Objects.requireNonNull(type) == EntitledType.HALF_DAY) {
                    halfDayUnits += units;
                } else if (Objects.requireNonNull(type) == EntitledType.HOURS){
                    hoursUnits += units;
                }
            }
            for (LeaveBalanceEntity leaveBalance : leaveBalanceMap.getOrDefault(userId, Collections.emptyList())){
                if (leaveBalance.getPolicy().getEntitledType() == EntitledType.HALF_DAY) {
                    totalUnitsAvailable += (int) (2*leaveBalance.getTotalUnits());
                    balanceUnits += (int) (2*leaveBalance.getBalanceUnits());
                }
                else {
                    totalUnitsAvailable += leaveBalance.getTotalUnits();
                    balanceUnits += leaveBalance.getBalanceUnits();
                }
            }
            for (TimeOffRequestEntity request : fixedRequestsMap.getOrDefault(userId, Collections.emptyList())){
                LocalDate monthStart = LocalDate.of(year, month, 1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
                LocalDate effectiveStart = request.getStartDate().isBefore(monthStart) ? monthStart : request.getStartDate();
                LocalDate effectiveEnd = request.getEndDate().isAfter(monthEnd) ? monthEnd : request.getEndDate();
                totalUnitsAvailable += request.getPolicy().getEntitledUnits();
                balanceUnits += balanceMap.getOrDefault(userId, 0.0);
                if (effectiveStart.equals(request.getStartDate()) && !effectiveEnd.equals(request.getEndDate())){
                    balanceUnits = balanceUnits - request.getEndDate().getDayOfMonth();
                }
            }
            log.info("Processing user: {}", userId);
            try {
            List<TimesheetEntity> timesheetEntities = timesheetAdapter.getTimesheetByUserIds(userId, year, month);
            log.info("reached loop");
            for (TimesheetEntity timesheet : timesheetEntities){
                try {
                    log.info("reached try");
                    if (Objects.equals(timesheet.getStatus().getStatusId(), TimesheetStatusEnum.PRESENT.getId())) {
                        LocalDate currentDate = timesheet.getDate();
                        if (!userWorkingDaysMap
                                .getOrDefault(userId, Collections.emptySet())
                                .contains(currentDate.getDayOfWeek())
                                || userHolidayMap
                                .getOrDefault(userId, Collections.emptySet())
                                .contains(currentDate)) {
                            continue;
                        }
                        presentUnits++;
                    } else if (Objects.equals(timesheet.getStatus().getStatusId(), TimesheetStatusEnum.PAID_LEAVE.getId())) {
                        paidLeavesTaken++;
                        fullDayUnits++;
                    }
                }catch (DateTimeException e) {
                    log.error("Failed to process timesheet for user in date: {} in {}/{}/{} - {}", userId,timesheet.getDate(), month, year, e.getMessage());
                }
            }
            } catch (DateTimeException e) {
                log.error("Failed to process timesheet for user: {} in {}/{} - {}", userId,month, year, e.getMessage());
                continue;
            }
            int restDays = calculateRestDays(userId, userWorkingDaysMap, userHolidayMap.getOrDefault(userId, Collections.emptySet()), daysInMonth, year, month);
            int totalWorkingDays = daysInMonth-restDays;
            unpaidLeavesTaken =  totalWorkingDays - (presentUnits+paidLeavesTaken-(halfDayUnits+hoursUnits));
            totalLeavesTaken = paidLeavesTaken + unpaidLeavesTaken;
            fullDayUnits += unpaidLeavesTaken;
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
            summaryEntity.setTotalPresentDays(presentUnits);
            summaryEntity.setTotalWorkingDays(totalWorkingDays);
            summaryEntity.setTotalHolidays(restDays);
            log.info("added summary");
            summaryEntityList.add(summaryEntity);
        }
        leaveBalanceAdapter.saveAllSummary(summaryEntityList);
        log.info("saved all summary");
    }

    public int calculateRestDays(String userId, Map<String, Set<DayOfWeek>> userWorkingDaysMap, Set<LocalDate> holidayDates,
                                 int daysInMonth, int year, int month) {
        int restDaysCount =0;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = LocalDate.of(year, month, day);
            if (!userWorkingDaysMap
                    .getOrDefault(userId, Collections.emptySet())
                    .contains(currentDate.getDayOfWeek())
                    || holidayDates
                    .contains(currentDate)) {
                restDaysCount++;
            }
        }
        return restDaysCount;
    }

    @Override
    public void updateDailyLeaveSummary(){
        LocalDate now = LocalDate.now(zoneId);
        LocalDate previousDay = now.minusDays(1);
        int day = previousDay.getDayOfMonth();
        int month = previousDay.getMonthValue();
        int year = previousDay.getYear();
        List<MonthlySummaryEntity> summaryEntityList = new ArrayList<>();
        log.info("getAll users");
        List<String> usersId = userAdapter.getAllActiveUsers();
        List<String> userIds = userPolicyAdapter.findAllUserIdsInUserPolicies(previousDay, usersId);
        Map<String, LocalDate> userHolidayMap = userAdapter.findUsersWithHolidayOnDate(previousDay, userIds)
                        .stream()
                        .collect(Collectors.toMap(
                                UserHolidayProjection::getUserId,
                                UserHolidayProjection::getDate
                        ));

        log.info("working day");
        TimesheetHelper.WorkScheduleResult result = timesheetHelper.fetchWorkSchedulesAndDays(userIds.toArray(new String[0]));
        Map<String, Set< DayOfWeek >> userWorkingDaysMap = result.getUserWorkingDaysMap();

        Map<String, TimesheetEntity> userTimesheetMap = timesheetAdapter.findAllTimesheetsByDate(previousDay)
                .stream()
                .collect(Collectors.toMap(
                        t -> t.getUser().getUserId(),
                        Function.identity()
                ));

        List<TimeOffRequestEntity> approvedRequests = timeOffRequestAdapter.findAllRequestByDate(previousDay, Status.APPROVED);
        Map<EntitledType, Set<String>> usersByEntitledType =
                approvedRequests.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.getPolicy().getEntitledType(),
                                () -> new EnumMap<>(EntitledType.class),
                                Collectors.mapping(
                                        r -> r.getUser().getUserId(),
                                        Collectors.toSet()
                                )
                        ));

        List<LeaveBalanceEntity> balances =
                leaveBalanceAdapter.findActiveBalances(previousDay);
        Map<String, List<LeaveBalanceEntity>> userLeaveBalanceMap =
                balances.stream()
                        .collect(Collectors.groupingBy(
                                lb -> lb.getUser().getUserId()
                        ));

        for (String userId : userIds){
            int paidLeavesTaken = 0;
            int unPaidLeavesTaken = 0;
            int fullDayUnits = 0;
            int totalUnitsAvailable = 0;
            int balanceUnits = 0;
            MonthlySummaryEntity monthlySummary =
                    (day == 1)
                            ? new MonthlySummaryEntity()
                            : leaveBalanceAdapter
                            .getMonthlySummary(userId, month, year)
                            .orElseGet(MonthlySummaryEntity::new);
            if (monthlySummary.getId() == null) {
                monthlySummary.setUserId(userId);
                monthlySummary.setYear(year);
                monthlySummary.setMonth(month);
            }
            if(!userWorkingDaysMap.get(userId).contains(previousDay.getDayOfWeek()) || userHolidayMap.containsKey(userId) ) {
                monthlySummary.setTotalHolidays(monthlySummary.getTotalHolidays() + 1);
            }
            else{
                monthlySummary.setTotalWorkingDays(monthlySummary.getTotalWorkingDays()+1);
                if (userTimesheetMap.containsKey(userId)){
                    monthlySummary.setTotalPresentDays(monthlySummary.getTotalPresentDays()+1);
                    if (usersByEntitledType.getOrDefault(EntitledType.HALF_DAY, Set.of()).contains(userId)){
                       monthlySummary.setHalfDayUnits(monthlySummary.getHalfDayUnits()+1);
                       paidLeavesTaken++;
                    }
                    else if (usersByEntitledType.getOrDefault(EntitledType.HOURS, Set.of()).contains(userId)){
                       monthlySummary.setHoursUnits(monthlySummary.getHoursUnits()+1);
                       paidLeavesTaken++;
                    }
                }
                else if (usersByEntitledType.getOrDefault(EntitledType.DAY, Set.of()).contains(userId)){
                       fullDayUnits++;
                       paidLeavesTaken++;
                }
                else {
                      monthlySummary.setUnpaidLeavesTaken(monthlySummary.getUnpaidLeavesTaken()+1);
                      unPaidLeavesTaken++;
                      fullDayUnits++;
                }
                monthlySummary.setFullDayUnits(monthlySummary.getFullDayUnits()+fullDayUnits);
                monthlySummary.setPaidLeavesTaken(monthlySummary.getPaidLeavesTaken()+paidLeavesTaken);
                monthlySummary.setTotalLeavesTaken(monthlySummary.getTotalLeavesTaken() + paidLeavesTaken + unPaidLeavesTaken);
            }
            for (LeaveBalanceEntity entity: userLeaveBalanceMap.getOrDefault(userId, Collections.emptyList())){
                  if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY){
                      totalUnitsAvailable +=(int) (2*entity.getTotalUnits());
                      balanceUnits += (int)(2*entity.getBalanceUnits());
                  }
                  else {
                      totalUnitsAvailable += (int) (1*entity.getTotalUnits());
                      balanceUnits += (int)(1*entity.getBalanceUnits());
                  }
            }
            monthlySummary.setTotalUnitsAvailable(totalUnitsAvailable);
            monthlySummary.setBalanceUnits(balanceUnits);
            summaryEntityList.add(monthlySummary);
        }
        leaveBalanceAdapter.saveAllSummary(summaryEntityList);
        log.info("saved summary");
    }

}
