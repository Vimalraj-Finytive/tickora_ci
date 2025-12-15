package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.CalendarHolidayProjection;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyKey;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyProjection;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.LeaveBalanceService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserCalendarProjection;
import com.uniq.tms.tms_microservice.shared.helper.TimesheetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
       private final CalendarAdapter calendarAdapter;
       private final TimesheetHelper timesheetHelper;
       private final UserAdapter userAdapter;

    public LeaveBalanceServiceImpl(LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper, TimeOffRequestAdapter timeOffRequestAdapter, UserPolicyAdapter userPolicyAdapter, TimesheetAdapter timesheetAdapter, CalendarAdapter calendarAdapter, TimesheetHelper timesheetHelper, UserAdapter userAdapter) {
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
        this.timeOffRequestAdapter = timeOffRequestAdapter;
        this.userPolicyAdapter = userPolicyAdapter;
        this.timesheetAdapter = timesheetAdapter;
        this.calendarAdapter = calendarAdapter;
        this.timesheetHelper = timesheetHelper;
        this.userAdapter = userAdapter;
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
        List<UserPolicyProjection> result = userPolicyAdapter.findUserPolicyValidTo(AccrualType.MONTHLY);
        updateLeaveBalance(currentBalances, AccrualType.MONTHLY, result);
    }

    private void updateLeaveBalance(List<LeaveBalanceEntity> currentBalances, AccrualType type, List<UserPolicyProjection> result){

        List<LeaveBalanceEntity> nextLeaveBalance = new ArrayList<>();
        Map<UserPolicyKey, LocalDate> validToMap = result.stream()
                .filter(p -> p.validTo() != null)
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
            double carry =  current.getPolicy().getMaxCarryForwardUnits()== null? 0.0:current.getPolicy().getMaxCarryForwardUnits() ;
            double expiredUnits = 0.0;
            double totalUnits = 0.0;
            if (type == AccrualType.MONTHLY) {
                log.info("monthly");
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
                if (validTo!= null && periodEnd.isAfter(validTo)){
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
            next.setActive(true);
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
        List<UserPolicyProjection> result = userPolicyAdapter.findUserPolicyValidTo(AccrualType.ANNUALLY);
        updateLeaveBalance(currentBalances, AccrualType.ANNUALLY, result);
    }

//    @Override
//    public void updateLeaveSummary() {
//
//        LocalDate now = LocalDate.now(zoneId);
//        LocalDate previousMonth = LocalDate.now(zoneId).minusMonths(1);
//        int month = previousMonth.getMonthValue();
//        int year = previousMonth.getYear();
//        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
//        List<MonthlySummaryEntity> summaryEntityList = new ArrayList<>();
//        log.info(" fetch userPolicy list");
//        List<String> usersId = userAdapter.getAllActiveUsers();
//        List<String> userIds = userPolicyAdapter.findAllUserIdsInUserPolicies(LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth()), usersId);
//        TimesheetHelper.WorkScheduleResult result = timesheetHelper.fetchWorkSchedulesAndDays(userIds.toArray(new String[0]));
//        Map<String, Set< DayOfWeek >> userWorkingDaysMap = result.getUserWorkingDaysMap();
//        List<UserCalendarProjection> userCalendarList = userAdapter.findCalendarIdsByUserIds(userIds.toArray(new String[0]));
//        List<CalendarHolidayProjection> rows = calendarAdapter.findAllHolidayDates();
//        Map<String, List<LocalDate>> calendarMap = rows.stream()
//                .collect(Collectors.groupingBy(
//                        CalendarHolidayProjection::getCalendarId,
//                        Collectors.mapping(CalendarHolidayProjection::getDate, Collectors.toList())
//                ));
//        Map<String, List<LocalDate>> userHolidayMap = userCalendarList.stream()
//                .collect(Collectors.toMap(
//                        UserCalendarProjection::getUserId,
//                        u -> calendarMap.get(u.getCalendarId())
//                ));
//
//        log.info("fetch monthly leaveBalance");
//        List<LeaveBalanceEntity> list =
//                leaveBalanceAdapter.findBalancesByMonthYearAndAccrualType(month, year, AccrualType.MONTHLY);
//
//        Map<String, List<LeaveBalanceEntity>> monthlyLeaveBalance =
//                list.stream()
//                        .collect(Collectors.groupingBy(lb -> lb.getUser().getUserId()));
//
//        log.info("fetch unpaid requests");
//        List<TimeOffRequestEntity> unpaidRequests = timeOffRequestAdapter.findAllUnpaidRequest( month, year, Compensation.UNPAID, Status.APPROVED);
//        Map<String, List<TimeOffRequestEntity>> unpaidMap =
//                unpaidRequests.stream()
//                        .collect(Collectors.groupingBy(r -> r.getUser().getUserId()));
//
//        log.info("fetch annual requests");
//        List<TimeOffRequestEntity> annualRequests = timeOffRequestAdapter.findAllAnnualRequests(month, year, Compensation.PAID, Status.APPROVED, AccrualType.ANNUALLY);
//        Map<String, List<TimeOffRequestEntity>> annualRequestsMap =
//                annualRequests.stream()
//                        .collect(Collectors.groupingBy(r -> r.getUser().getUserId()));
//
//        log.info("fetch annual leaveBalance");
//        List<LeaveBalanceEntity> leaveBalanceEntities = leaveBalanceAdapter.findBalancesByYearAndAccrualType(year, AccrualType.ANNUALLY);
//        Map<String, List<LeaveBalanceEntity>> leaveBalanceMap =
//                leaveBalanceEntities.stream()
//                        .collect(Collectors.groupingBy(lb -> lb.getUser().getUserId()));
//
//        log.info("fetch fixed requests");
//        List<TimeOffRequestEntity> fixedRequests = timeOffRequestAdapter.findFixedRequests(month, year, Status.APPROVED, AccrualType.FIXED);
//        Map<String, List<TimeOffRequestEntity>> fixedRequestsMap =
//                fixedRequests.stream()
//                        .collect(Collectors.groupingBy(
//                                r -> r.getUser().getUserId()));
//
//        log.info("fetch fixed leaveBalance");
//        List<LeaveBalanceEntity> fixedLeaveBalance = leaveBalanceAdapter.findAllFixedAccrual(month, year, AccrualType.FIXED);
//        Map<String, Double> balanceMap = fixedLeaveBalance.stream()
//                .collect(Collectors.toMap(
//                        lb -> lb.getUser().getUserId(),
//                        lb -> lb.getBalanceUnits() != null ? lb.getBalanceUnits() : 0.0,
//                        (existing, replacement) -> existing
//                ));
//
//        for (String userId : userIds){
//            log.info("loop starts");
//            MonthlySummaryEntity summaryEntity = new MonthlySummaryEntity();
//            int totalLeavesTaken = 0;
//            int paidLeavesTaken = 0;
//            int unpaidLeavesTaken = 0;
//            int totalUnitsAvailable = 0;
//            int balanceUnits = 0;
//            int halfDayUnits = 0;
//            int fullDayUnits = 0;
//            int hoursUnits = 0;
//            for (LeaveBalanceEntity entity : monthlyLeaveBalance.getOrDefault(userId, Collections.emptyList())){
//                if (entity.getPolicy().getEntitledType() == EntitledType.DAY){
//                    fullDayUnits += entity.getLeaveTakenUnits();
//                    paidLeavesTaken += entity.getLeaveTakenUnits();
//                    totalUnitsAvailable += entity.getTotalUnits();
//                    balanceUnits += entity.getBalanceUnits();
//                }
//                else if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY){
//                    halfDayUnits += (int)(entity.getLeaveTakenUnits()*2);
//                    paidLeavesTaken += (int)(entity.getLeaveTakenUnits()*2);
//                    totalUnitsAvailable += (int)(entity.getTotalUnits()*2);
//                    balanceUnits += entity.getBalanceUnits();
//                }
//                else {
//                    hoursUnits += entity.getLeaveTakenUnits();
//                    paidLeavesTaken += entity.getLeaveTakenUnits();
//                    totalUnitsAvailable += entity.getTotalUnits();
//                    balanceUnits += entity.getBalanceUnits();
//                }
//            }
//            for (TimeOffRequestEntity request : unpaidMap.getOrDefault(userId, Collections.emptyList())){
//                unpaidLeavesTaken += request.getUnitsRequested();
//                fullDayUnits += request.getUnitsRequested();
//            }
//            for (TimeOffRequestEntity entity : annualRequestsMap.getOrDefault(userId, Collections.emptyList())){
//                if (entity.getPolicy().getEntitledType() == EntitledType.DAY){
//                    fullDayUnits += entity.getUnitsRequested();
//                    paidLeavesTaken += entity.getUnitsRequested();
//                }
//                else if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY){
//                    halfDayUnits += (2*entity.getUnitsRequested());
//                    paidLeavesTaken +=(2*entity.getUnitsRequested());
//                }
//                else {
//                    hoursUnits += entity.getUnitsRequested();
//                    paidLeavesTaken += entity.getUnitsRequested();
//                }
//            }
//            for (LeaveBalanceEntity leaveBalance : leaveBalanceMap.getOrDefault(userId, Collections.emptyList())){
//                totalUnitsAvailable += leaveBalance.getTotalUnits();
//                balanceUnits += leaveBalance.getBalanceUnits();
//            }
//            for (TimeOffRequestEntity request : fixedRequestsMap.getOrDefault(userId, Collections.emptyList())){
//                LocalDate monthStart = LocalDate.of(year, month, 1);
//                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
//                LocalDate effectiveStart = request.getStartDate().isBefore(monthStart) ? monthStart : request.getStartDate();
//                LocalDate effectiveEnd = request.getEndDate().isAfter(monthEnd) ? monthEnd : request.getEndDate();
//                int days = (int)ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;
//                fullDayUnits += days;
//                paidLeavesTaken += fullDayUnits;
//                totalUnitsAvailable += request.getPolicy().getEntitledUnits();
//                balanceUnits += balanceMap.getOrDefault(userId, 0.0);
//                if (effectiveStart.equals(request.getStartDate()) && !effectiveEnd.equals(request.getEndDate())){
//                    balanceUnits = balanceUnits - request.getEndDate().getDayOfMonth();
//                }
//            }
//            int restDays = calculateRestDays(userId, userWorkingDaysMap, userHolidayMap.get(userId), daysInMonth, year, month);
//            List<TimesheetEntity> timesheetEntities = timesheetAdapter.getTimesheetByUserIds(userId, year, month);
//            totalLeavesTaken = paidLeavesTaken + unpaidLeavesTaken;
//            summaryEntity.setUserId(userId);
//            summaryEntity.setYear(year);
//            summaryEntity.setMonth(month);
//            summaryEntity.setTotalLeavesTaken(totalLeavesTaken);
//            summaryEntity.setPaidLeavesTaken(paidLeavesTaken);
//            summaryEntity.setUnpaidLeavesTaken(unpaidLeavesTaken);
//            summaryEntity.setFullDayUnits(fullDayUnits);
//            summaryEntity.setHalfDayUnits(halfDayUnits);
//            summaryEntity.setTotalUnitsAvailable(totalUnitsAvailable);
//            summaryEntity.setBalanceUnits(balanceUnits);
//            summaryEntity.setHoursUnits(hoursUnits);
//            summaryEntity.setTotalPresentDays(timesheetEntities.size());
//            summaryEntity.setTotalWorkingDays(daysInMonth-restDays);
//            log.info("added summary");
//            summaryEntityList.add(summaryEntity);
//        }
//        leaveBalanceAdapter.saveAllSummary(summaryEntityList);
//        log.info("saved all summary");
//    }

//    public int calculateRestDays(String userId, Map<String, Set<DayOfWeek>> userWorkingDaysMap, List<LocalDate> holidayDates,
//                                 int daysInMonth, int year, int month) {
//        int restDaysCount =0;
//        for (int day = 1; day <= daysInMonth; day++) {
//            LocalDate currentDate = LocalDate.of(year, month, day);
//            if(!userWorkingDaysMap.get(userId).contains(currentDate.getDayOfWeek()) || holidayDates.contains(currentDate) ) {
//                restDaysCount++;
//            }
//        }
//        return restDaysCount;
//    }

    @Override
    public void updateLeaveSummary(){
        LocalDate now = LocalDate.now(zoneId);
        LocalDate previousDay = now.minusDays(1);
        int day = previousDay.getDayOfMonth();
        int month = previousDay.getMonthValue();
        int year = previousDay.getYear();
        List<MonthlySummaryEntity> summaryEntityList = new ArrayList<>();
        log.info("getAll users");
        List<String> usersId = userAdapter.getAllActiveUsers();
        List<String> userIds = userPolicyAdapter.findAllUserIdsInUserPolicies(previousDay, usersId);
        List<CalendarHolidayProjection> calendarHolidayList = calendarAdapter.findHolidayCalendarsByDate(previousDay);
        List<UserCalendarProjection> userCalendarList = userAdapter.findCalendarIdsByUserIds(userIds.toArray(new String[0]));
        Map<String, LocalDate> calendarHolidayMap =
                calendarHolidayList.stream()
                        .collect(Collectors.toMap(
                                CalendarHolidayProjection::getCalendarId,
                                CalendarHolidayProjection::getDate
                        ));

        log.info("user holiday map");
        Map<String, LocalDate> userHolidayMap =
                userCalendarList.stream()
                        .filter(uc -> calendarHolidayMap.containsKey(uc.getCalendarId()))
                        .collect(Collectors.toMap(
                                UserCalendarProjection::getUserId,
                                uc -> calendarHolidayMap.get(uc.getCalendarId())
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
