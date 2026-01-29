package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffRequestAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.UserPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.TimeOffExportView;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffRequestService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.TimesheetStatusEnum;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.MemberType;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserHolidayProjection;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FixedWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FlexibleWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.DayOfWeekEnum;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.WorkScheduleTypeEnum;
import com.uniq.tms.tms_microservice.shared.dto.EnumModel;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.shared.helper.TimesheetHelper;
import com.uniq.tms.tms_microservice.shared.util.DateTimeUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

    private static final Logger log = LoggerFactory.getLogger(TimeOffRequestServiceImpl.class);
    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    private final TimeOffRequestAdapter timeOffRequestAdapter;
    private final TimeOffPolicyEntityMapper TimeOffPolicyEntityMapper;
    private final LeaveBalanceAdapter leaveBalanceAdapter;
    private final TimeOffPolicyAdapter timeOffPolicyAdapter;
    private final UserPolicyAdapter userPolicyAdapter;
    private final AuthHelper authHelper;
    private final UserAdapter userAdapter;
    private final TimesheetAdapter timesheetAdapter;
    private final TimesheetService timesheetService;
    private final TimesheetHelper timesheetHelper;

    public TimeOffRequestServiceImpl(TimeOffRequestAdapter timeOffRequestAdapter, TimeOffPolicyEntityMapper TimeOffPolicyEntityMapper,
                                     LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyAdapter timeOffPolicyAdapter, UserPolicyAdapter userPolicyAdapter,
                                     AuthHelper authHelper, UserAdapter userAdapter, TimesheetAdapter timesheetAdapter, TimesheetService timesheetService, TimesheetHelper timesheetHelper) {
        this.timeOffRequestAdapter = timeOffRequestAdapter;
        this.TimeOffPolicyEntityMapper = TimeOffPolicyEntityMapper;
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
        this.userPolicyAdapter = userPolicyAdapter;
        this.authHelper = authHelper;
        this.userAdapter = userAdapter;
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetService = timesheetService;
        this.timesheetHelper = timesheetHelper;
    }

    @Override
    @Transactional
    public void createRequest(TimeOffRequest request) {
        String policyId = request.getPolicyId();
        String userId = request.getUserId();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        boolean policyActive;
        boolean policyValidForDate;
        boolean userPolicyActive;
        boolean userPolicyDateValid;

        policyActive = timeOffPolicyAdapter.isPolicyActive(policyId);
        if (!policyActive) {
            throw new IllegalStateException("Selected policy is inactive.");
        }
        policyValidForDate = timeOffPolicyAdapter.existsValidPolicy(policyId, startDate, endDate);
        if (!policyValidForDate) {
            throw new IllegalStateException("The selected policy is not valid for the specified date range.");
        }
        userPolicyActive = userPolicyAdapter.isUserPolicyActive(userId, policyId);
        if (!userPolicyActive) {
            throw new IllegalStateException("The selected policy is not assigned to this user.");
        }
        userPolicyDateValid = userPolicyAdapter.existsValidUserPolicy(policyId, userId, startDate, endDate);
        if (!userPolicyDateValid) {
            throw new IllegalStateException("User does not have policy assignment for the selected date range.");
        }
        boolean exists = timeOffRequestAdapter.existsTimeoffRequest(request.getUserId(), request.getPolicyId(), LocalDate.now(zoneId));
        if (exists) {
            throw new IllegalArgumentException("Request for today is already pending or approved");
        }
        if (LocalDate.now(zoneId).isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("Invalid request format for the selected entitled type");
        }
        boolean overlap = timeOffRequestAdapter.existsOverlappingRequest(request.getUserId(), request.getPolicyId(), request.getStartDate(), request.getEndDate());
        if (overlap) {
            throw new IllegalArgumentException("A leave request for the selected dates already exists. Duplicate requests are not allowed.");
        }
        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findPolicyById(request.getPolicyId());
        UserEntity user = userAdapter.getUserById(request.getUserId());
        if (user.getRequestApproverId() == null || user.getRequestApproverId().isEmpty()) {
            throw new IllegalArgumentException("No request approver is associated with your profile. Please contact your admin to proceed.");
        }
        UserEntity approver = userAdapter.findUserByOrgIdAndUserId(authHelper.getOrgId(), user.getRequestApproverId());
        if (approver.isActive() == null || !approver.isActive()) {
            throw new IllegalArgumentException("Your request approver is inactive. Please contact your admin.");
        }
        WorkScheduleEntity workSchedule = user.getWorkSchedule();
        if (workSchedule == null) {
            throw new IllegalArgumentException(
                    "Work schedule is not assigned. Please contact your admin."
            );
        }
        double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (policy.getEntitledType() == EntitledType.DAY && days != request.getUnitsRequested()) {
            throw new IllegalArgumentException("The specified units do not align with the selected date range.");
        }
        boolean isHoliday = checkIsHoliday(request.getUserId(), request.getStartDate(), request.getEndDate());
        if (isHoliday){
            throw new IllegalArgumentException("Cannot create leave request on a holiday");
        }
        TimeOffRequestEntity entity = new TimeOffRequestEntity();
        Integer requested = 0;
        if (policy.getCompensation() == Compensation.PAID) {
            LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(policy.getPolicyId(), request.getUserId(), request.getStartDate(), request.getEndDate());
            if (leaveBalance == null){
                throw new IllegalArgumentException("Cannot create leave request. Leave balance is not available for the selected period.");
            }
            if (leaveBalance.getBalanceUnits() == 0.0) {
                throw new IllegalArgumentException("Insufficient leave balance to apply for paid leave.");
            }
            boolean invalidDayOrHalfDay =
                    (policy.getEntitledType() == EntitledType.DAY || policy.getEntitledType() == EntitledType.HALF_DAY)
                            && (request.getStartTime() != null || request.getEndTime() != null);
            boolean invalidHour =
                    policy.getEntitledType() == EntitledType.HOURS
                            && (days != 1 ||
                            request.getStartTime() == null || request.getEndTime() == null);
            boolean invalidHalfDay =
                    policy.getEntitledType() == EntitledType.HALF_DAY
                            && days != 1 && request.getUnitsRequested() != 1;
            if (invalidDayOrHalfDay || invalidHour || invalidHalfDay) {
                throw new IllegalArgumentException("Invalid request format for the selected entitled type");
            }
            if (policy.getEntitledType() == EntitledType.DAY) {
                requested = request.getUnitsRequested();
                validateDays(days, leaveBalance.getBalanceUnits());
            } else if (policy.getEntitledType() == EntitledType.HALF_DAY) {
                if (request.getHourType() == null) {
                    throw new IllegalArgumentException("Hour type is required for half day permission");
                }
                requested = request.getUnitsRequested();
                days = request.getUnitsRequested() * 0.5;
                validateDays(days, leaveBalance.getBalanceUnits());
                setTimeForHalf(workSchedule, request.getStartDate(), request.getHourType(), entity);
            } else {
                requested = request.getUnitsRequested();
                double hours = validateHours(workSchedule, request.getStartTime(), request.getEndTime(), request.getUnitsRequested(), leaveBalance, request.getStartDate());
                if (hours > leaveBalance.getBalanceUnits()) {
                    throw new IllegalArgumentException("Insufficient leave balance.");
                }
                entity.setStartTime(request.getStartTime());
                entity.setEndTime(request.getEndTime());
            }
        }
        entity.setUser(user);
        entity.setPolicy(policy);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setUnitsRequested(request.getUnitsRequested());
        entity.setHourType(request.getHourType() != null ? request.getHourType() : null);
        entity.setStatus(Status.PENDING);
        entity.setReason(request.getReason());
        entity.setRequestDate(LocalDate.now(zoneId));
        List<Long> groupIds = user.getUserGroups().stream()
                .map(g -> g.getGroup().getGroupId())
                .toList();
        Set<String> viewers =
                userAdapter.getAllSupervisorIds(groupIds, user.getUserId(), MemberType.SUPERVISOR.getValue())
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        log.info("viewers size{}", viewers.size());
        List<String> superAdminIds = userAdapter.findSuperAdminByOrgId(authHelper.getOrgId())
                .stream()
                .map(UserEntity::getUserId)
                .toList();

        List<String> activeSuperAdmins = superAdminIds.stream()
                .map(userAdapter::getUserById)
                .filter(UserEntity::isActive)
                .map(UserEntity::getUserId)
                .toList();

        if (activeSuperAdmins.isEmpty()) {
            throw new IllegalArgumentException("No active super admin found. Please contact system administrator.");
        }

        viewers.remove(user.getRequestApproverId());
        activeSuperAdmins.forEach(viewers::remove);

        TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
        deductLeaveBalance(saved, requested);

        List<UsersRequestMappingEntity> usersMapping =
                Stream.concat(
                        activeSuperAdmins.stream()
                                .map(superAdminId -> buildMapping(ViewerType.APPROVER, superAdminId, user.getUserId(), saved.getTimeOffRequestId())),
                        Stream.concat(
                                Stream.of(buildMapping(ViewerType.APPROVER, user.getRequestApproverId(), user.getUserId(), saved.getTimeOffRequestId())),
                                viewers.stream().map(viewer ->
                                        buildMapping(ViewerType.VIEWER, viewer, user.getUserId(), saved.getTimeOffRequestId()))
                        )
                ).toList();
        List<UsersRequestMappingEntity> entities = timeOffRequestAdapter.saveUsersRequestMapping(usersMapping);
    }

    private UsersRequestMappingEntity buildMapping(ViewerType type, String viewerId, String requesterId, Long requestId) {
        UsersRequestMappingEntity m = new UsersRequestMappingEntity();
        m.setType(type);
        m.setViewerId(viewerId);
        m.setRequesterId(requesterId);
        m.setTimeOffRequestId(requestId);
        return m;
    }

    private boolean checkIsHoliday(String userId, LocalDate startDate, LocalDate endDate){
        if (startDate == null || endDate == null){
            return false;
        }
        TimesheetHelper.WorkScheduleResult result = timesheetHelper.fetchWorkSchedulesAndDays(new String[]{ userId });
        Map<String, Set< DayOfWeek >> userWorkingDaysMap = result.getUserWorkingDaysMap();
        List<UserHolidayProjection> results =
                userAdapter.findUserHolidays(List.of(userId));

        Map<String, List<LocalDate>> userHolidayMap = results.stream()
                .collect(Collectors.groupingBy(
                        UserHolidayProjection::getUserId,
                        Collectors.mapping(
                                UserHolidayProjection::getDate,
                                Collectors.toList()
                        )
                ));
        List<LocalDate> holidays = userHolidayMap.get(userId);
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (!userWorkingDaysMap.get(userId).contains(dayOfWeek) || (holidays != null && holidays.contains(date))){
                return true;
            }
        }
        return false;
    }

    private double validateHours(WorkScheduleEntity workSchedule, LocalTime startTime, LocalTime endTime, Integer hoursRequested, LeaveBalanceEntity leaveBalance, LocalDate date) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        DayOfWeekEnum day = DayOfWeekEnum.valueOf(date.getDayOfWeek().name());
        if (workSchedule.getType().getType() == WorkScheduleTypeEnum.FIXED) {
            FixedWorkScheduleEntity fixedEntity =
                    timesheetAdapter.findByWorkScheduleIdAndDay(workSchedule.getScheduleId(), day);
            DateTimeUtil.validateDuration(minutes, hoursRequested, fixedEntity.getDuration());
            LocalTime fixedStartTime = fixedEntity.getStartTime().toLocalTime();
            LocalTime fixedEndTime = fixedEntity.getEndTime().toLocalTime();
            boolean crossesMidnight = fixedEndTime.isBefore(fixedStartTime);
            boolean inRange;
            if (crossesMidnight) {
                inRange = (startTime.equals(fixedStartTime) || startTime.isAfter(fixedStartTime) || startTime.isBefore(fixedEndTime)) &&
                        (endTime.equals(fixedEndTime) || endTime.isAfter(fixedStartTime) || endTime.isBefore(fixedEndTime));
            } else {
                inRange = (startTime.equals(fixedStartTime) || startTime.isAfter(fixedStartTime)) && (endTime.equals(fixedEndTime) || endTime.isBefore(fixedEndTime));
            }
            if (!inRange) {
                throw new IllegalArgumentException("Start and End times must be within the workschedule range");
            }
        } else {
            FlexibleWorkScheduleEntity flexibleEntity =
                    timesheetAdapter.findByWorkScheduleIdAndDays(workSchedule.getScheduleId(), day);
            DateTimeUtil.validateDuration(minutes, hoursRequested, flexibleEntity.getDuration());
        }
        return minutes / 60.0;
    }

    private void validateDays(double days, Double balanceUnits) {
        if (balanceUnits - days < 0) {
            throw new IllegalArgumentException("Insufficient leave balance.");
        }
    }

    private void setTimeForHalf(WorkScheduleEntity workSchedule, LocalDate startDate, HourType leaveHalf, TimeOffRequestEntity entity) {
        DayOfWeekEnum day = DayOfWeekEnum.valueOf(startDate.getDayOfWeek().name());
        if (workSchedule.getType().getType() == WorkScheduleTypeEnum.FIXED) {
            FixedWorkScheduleEntity fixedEntity =
                    timesheetAdapter.findByWorkScheduleIdAndDay(workSchedule.getScheduleId(), day);
            double half = fixedEntity.getDuration() / 2;
            int hours = (int) half;
            int minutes = (int) ((half - hours) * 60);
            LocalTime halfTime = fixedEntity.getStartTime().toLocalTime().plusHours(hours).plusMinutes(minutes);
            if (leaveHalf == HourType.FIRST_HALF) {
                entity.setStartTime(fixedEntity.getStartTime().toLocalTime());
                entity.setEndTime(halfTime);
            } else {
                entity.setStartTime(halfTime);
                entity.setEndTime(fixedEntity.getEndTime().toLocalTime());
            }
        }
    }

    @Override
    @Transactional
    public void employeeUpdateStatus(EmployeeStatusUpdate model) {
        TimeOffRequestEntity entity = timeOffRequestAdapter.findByRequestId(model.getRequestId());
        WorkScheduleEntity workSchedule = entity.getUser().getWorkSchedule();
        if (LocalDate.now(zoneId).isAfter(entity.getStartDate()) ||
                (model.getStatus() != null && !handleEmployeeRules(entity.getStatus(), model.getStatus()))) {
            throw new IllegalArgumentException("Update not allowed because the leave start date has already passed.");
        }
        if (model.getStatus() != null && model.getStatus() == Status.CANCELLED) {
            if (entity.getPolicy().getCompensation() == Compensation.PAID) {
                Integer requested = entity.getUnitsRequested();
                addLeaveBalance(entity, requested);
            }
            entity.setStatus(model.getStatus());
            timeOffRequestAdapter.saveRequest(entity);
            timesheetService.rollbackLeaveTimesheet(entity);
            return;
        }
        boolean isHoliday = checkIsHoliday(model.getUserId(), model.getStartDate(), model.getEndDate());
        if (isHoliday){
            throw new IllegalArgumentException("Cannot create leave request on a holiday");
        }
        double days = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;
        if (model.getReason() != null) {
            entity.setReason(model.getReason());
        }
        if (model.getStartDate() != null && model.getEndDate() != null) {
            days = ChronoUnit.DAYS.between(model.getStartDate(), model.getEndDate()) + 1;
            entity.setStartDate(model.getStartDate());
            entity.setEndDate(model.getEndDate());
        }
        if (entity.getPolicy().getCompensation() == Compensation.UNPAID) {
            if (model.getUnitsRequested() != null && days != model.getUnitsRequested()) {
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            entity.setUnitsRequested(model.getUnitsRequested());
            timeOffRequestAdapter.saveRequest(entity);
            return;
        }
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUser().getUserId(), entity.getStartDate(), entity.getEndDate());
        if ((entity.getPolicy().getEntitledType() == EntitledType.DAY ||
                entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) &&
                (model.getUnitsRequested() != null && model.getStartDate() != null && model.getEndDate() != null)) {
            if (model.getUnitsRequested() != days) {
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            if (entity.getPolicy().getEntitledType() == EntitledType.DAY) {
                if (model.getUnitsRequested() > entity.getUnitsRequested()) {
                    days = model.getUnitsRequested() - entity.getUnitsRequested();
                    validateDays(days, leaveBalance.getBalanceUnits());
                }
            }
            if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) {
                if (days != 1) {
                    throw new IllegalArgumentException("Invalid paid leave request.");
                }
                if (model.getHourType() != null) {
                    entity.setHourType(model.getHourType());
                }
                setTimeForHalf(workSchedule, model.getStartDate(), model.getHourType(), entity);
            }
        }

        if (entity.getPolicy().getEntitledType() == EntitledType.HOURS && (model.getStartTime() != null && model.getEndTime() != null && model.getUnitsRequested() != null)) {
            if (days != 1) {
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            double modelUnitsRequested = validateHours(workSchedule, model.getStartTime(), model.getEndTime(), model.getUnitsRequested(), leaveBalance, model.getStartDate());
            if (model.getUnitsRequested() > entity.getUnitsRequested()) {
                double hour = modelUnitsRequested - entity.getUnitsRequested();
                if (leaveBalance.getBalanceUnits() < hour) {
                    throw new IllegalArgumentException("Insufficient leave balance.");
                }
            }
            entity.setStartTime(model.getStartTime());
            entity.setEndTime(model.getEndTime());
        }

        if (model.getUnitsRequested() > entity.getUnitsRequested()) {
            deductLeaveBalance(entity, model.getUnitsRequested() - entity.getUnitsRequested());
        } else if (model.getUnitsRequested() < entity.getUnitsRequested()) {
            addLeaveBalance(entity, entity.getUnitsRequested() - model.getUnitsRequested());
        }
        entity.setUnitsRequested(model.getUnitsRequested());
        TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
    }

    private void checkCredentials(String policyId, LocalDate requestDate) {
        if (policyId == null || requestDate == null) {
            throw new IllegalArgumentException("Missing required fields");
        }
    }

    private boolean handleEmployeeRules(Status current, Status next) {
        return ((current == Status.PENDING || current == Status.APPROVED) && (next == Status.CANCELLED) ||
                (current == Status.PENDING && next == Status.PENDING));
    }

    @Override
    @Transactional
    public void adminUpdateStatus(AdminStatusUpdate model) {
        TimeOffRequestEntity entity =
                timeOffRequestAdapter.findByRequestId(model.getRequestId());
        boolean dateInvalid =
                LocalDate.now(zoneId).isAfter(entity.getStartDate());
        boolean statusInvalid =
                model.getStatus() != null &&
                        !handleAdminRules(entity.getStatus(), model.getStatus());
        if (dateInvalid || statusInvalid) {
            log.warn("Update not allowed. Date invalid = {}, Status invalid = {}",
                    dateInvalid, statusInvalid);
            throw new IllegalArgumentException(
                    "Update not allowed because the leave start date has already passed."
            );
        }
        if (model.getStatus() == null) {
            return;
        }
        log.info("Updating status from {} → {}", entity.getStatus(), model.getStatus());
        entity.setStatus(model.getStatus());
        TimeOffRequestEntity saved =
                timeOffRequestAdapter.saveRequest(entity);
        log.info("Status updated successfully. Saved status: {}", saved.getStatus());
        if (saved.getStatus() == Status.APPROVED) {
            boolean isPaid =
                    entity.getPolicy().getCompensation() == Compensation.PAID;
            EntitledType entitledType =
                    entity.getPolicy().getEntitledType();
            if (entitledType == EntitledType.DAY) {
                TimesheetStatusEnum status =
                        isPaid
                                ? TimesheetStatusEnum.PAID_LEAVE
                                : TimesheetStatusEnum.UNPAID_LEAVE;
                log.info("Applying {} leave for date range {} → {}",
                        status, entity.getStartDate(), entity.getEndDate());
                timesheetService.createTimesheet(
                        status,
                        entity.getUser().getUserId(),
                        entity.getStartDate(),
                        entity.getEndDate(),
                        entity.getStartTime(),
                        entity.getEndTime()
                );
            } else if (entitledType == EntitledType.HALF_DAY) {
                log.info("Applying HALF_DAY leave");
                timesheetService.createTimesheet(
                        TimesheetStatusEnum.HALF_DAY,
                        entity.getUser().getUserId(),
                        entity.getStartDate(),
                        entity.getEndDate(),
                        entity.getStartTime(),
                        entity.getEndTime()
                );
            } else if (entitledType == EntitledType.HOURS) {
                log.info("Applying PERMISSION (hours-based leave)");
                timesheetService.createTimesheet(
                        TimesheetStatusEnum.PERMISSION,
                        entity.getUser().getUserId(),
                        entity.getStartDate(),
                        entity.getEndDate(),
                        entity.getStartTime(),
                        entity.getEndTime()
                );
            }
        } else if (saved.getStatus() == Status.REJECTED) {
            log.info("Request REJECTED → Restoring leave balance and deleting timesheet");
            Integer requested = saved.getUnitsRequested();
            addLeaveBalance(saved, requested);
            timesheetService.rollbackLeaveTimesheet(saved);
            log.info("Leave balance restored and timesheet deleted");
        }
        log.info("adminUpdateStatus completed successfully for User: {}, Policy: {}",
                entity.getUser().getUserId(),
                entity.getPolicy().getPolicyId());
    }

    private boolean handleAdminRules(Status current, Status next) {
        return switch (current) {
            case PENDING -> next == Status.APPROVED || next == Status.REJECTED || next == Status.PENDING;
            case APPROVED -> next == Status.REJECTED ;
            default -> false;
        };
    }

    public void deductLeaveBalance(TimeOffRequestEntity entity, Integer requested) {
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUser().getUserId(), entity.getStartDate(), entity.getEndDate());
        if (leaveBalance != null && entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) {
            log.info("leave balance find");
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() + requested * 0.5);
            double balanceUnits = leaveBalance.getBalanceUnits() - requested * 0.5;
            leaveBalance.setBalanceUnits(balanceUnits);
            leaveBalanceAdapter.saveLeaveBalance(leaveBalance);
        } else if (leaveBalance != null && (entity.getPolicy().getEntitledType() == EntitledType.HOURS ||
                entity.getPolicy().getEntitledType() == EntitledType.DAY)) {
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() + requested);
            double balanceUnits = leaveBalance.getBalanceUnits() - requested;
            leaveBalance.setBalanceUnits(balanceUnits);
            leaveBalanceAdapter.saveLeaveBalance(leaveBalance);
        }
    }

    public void addLeaveBalance(TimeOffRequestEntity entity, Integer requested) {
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUser().getUserId(), entity.getStartDate(), entity.getEndDate());
        if (leaveBalance != null && entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) {
            log.info("leave balance finds");
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() - requested * 0.5);
            double balanceUnits = leaveBalance.getBalanceUnits() + requested * 0.5;
            leaveBalance.setBalanceUnits(balanceUnits);
            leaveBalanceAdapter.saveLeaveBalance(leaveBalance);
        } else if (leaveBalance != null && (entity.getPolicy().getEntitledType() == EntitledType.HOURS ||
                entity.getPolicy().getEntitledType() == EntitledType.DAY)) {
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() - requested);
            double balanceUnits = leaveBalance.getBalanceUnits() + requested;
            leaveBalance.setBalanceUnits(balanceUnits);
            leaveBalanceAdapter.saveLeaveBalance(leaveBalance);
        }
    }

    @Override
    public List<EnumModel> getStatus() {
        List<EnumModel> list = new ArrayList<>();
        for (Status e : Status.values()) {
            EnumModel model = new EnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public List<TimeOffRequestResponseModel> filterRequestsByRole(
            LocalDate fromDate,
            LocalDate toDate,
            int minRoleLevel) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }
        List<TimeOffRequestUserModel> list =
                timeOffRequestAdapter.filterWithUserAndRole(fromDate, toDate, minRoleLevel);
        List<TimeOffRequestResponseModel> result = new ArrayList<>();
        for (TimeOffRequestUserModel item : list) {
            TimeOffRequestResponseModel rm =
                    TimeOffPolicyEntityMapper.toModel(item.getRequest());
            rm.setUserName(item.getUserName());
            result.add(rm);
        }
        return result;
    }

    @Override
    public List<TimeOffExportModel> filterRequests(TimeOffExportRequest request, String loggedUserId) {

        // fetch rows from view
        List<TimeOffExportView> rows = fetchExportRows(request, loggedUserId);

        // final result map
        Map<Long, TimeOffExportModel> result = new LinkedHashMap<>();

        // Track duplicates for viewers
        Map<String, Long> addedCount = new HashMap<>();

        // group view rows using requestId
        Map<String, Long> requestKey = rows.stream()
                .filter(r -> r.getViewerId() != null && r.getViewerType() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getTimeoffRequestId()
                                + "|" + r.getViewerId()
                                + "|" + r.getViewerType(),
                        Collectors.counting()
                ));

        for (TimeOffExportView row : rows) {

            Long requestId = row.getTimeoffRequestId();

            // create new model if not exists
            TimeOffExportModel model = result.get(requestId);
            if (model == null) {
                model = new TimeOffExportModel();
                model.setTimeoffRequestId(row.getTimeoffRequestId());
                model.setUserId(row.getCreatorId());
                model.setUserName(row.getCreatorName());
                model.setPolicyName(row.getPolicyName());
                model.setPolicyId(row.getPolicyId());
                model.setRequestDate(LocalDate.parse(row.getRequestedDate()));
                model.setStartDate(row.getLeaveStartDate());
                model.setEndDate(row.getLeaveEndDate());
                model.setStartTime(row.getLeaveStartTime());
                model.setEndTime(row.getLeaveEndTime());
                model.setUnitsRequested(Double.valueOf(row.getUnitsRequested()));
                model.setReason(row.getReason());
                model.setStatus(row.getStatus());
                model.setLeaveType(row.getLeaveType());
                model.setHourType(row.getHourType());
                model.setViewers(new ArrayList<>());
                model.setApprover(new ArrayList<>());

                result.put(requestId, model);
            }

            // If no viewer → skip
            if (row.getViewerId() == null || row.getViewerType() == null) {
                continue;
            }

            UserEntity viewerUser =
                    userAdapter.findById(row.getViewerId()).orElse(null);

            if (viewerUser != null
                    && viewerUser.getRole().getHierarchyLevel()
                    == UserRole.SUPERADMIN.getHierarchyLevel()) {
                continue;
            }

            // Unique viewer entry key
            String viewerKey = requestId + "|" + row.getViewerId() + "|" + row.getViewerType();

            long dbTotal = requestKey.getOrDefault(viewerKey, 1L);
            long added = addedCount.getOrDefault(viewerKey, 0L);

            if (added < dbTotal) {
                ViewerModel vm = new ViewerModel();
                vm.setUserId(row.getViewerId());
                vm.setUserName(row.getViewerName());
                vm.setViewerType(row.getViewerType());

                if (ViewerType.VIEWER.getValue().equalsIgnoreCase(row.getViewerType())) {
                    model.getViewers().add(vm);
                } else {
                    model.getApprover().add(vm);
                }

                addedCount.put(viewerKey, added + 1);
            }
        }

        return new ArrayList<>(result.values());
    }

    public List<TimeOffExportView> fetchExportRows(TimeOffExportRequest request, String loggedUserId) {

        String[] statusArr = request.getStatus() == null ? new String[0] : request.getStatus().toArray(new String[0]);
        String[] policyArr = request.getPolicyIds() == null ? new String[0] : request.getPolicyIds().toArray(new String[0]);


        UserEntity user = userAdapter.findById(loggedUserId).orElseThrow();

        boolean isSuperAdmin = user.getRole().getHierarchyLevel() == UserRole.SUPERADMIN.getHierarchyLevel();
        boolean hasUserId = request.getUserId() != null && !request.getUserId().trim().isEmpty();
        boolean creatorMode = hasUserId && request.getUserId().equals(loggedUserId);

        if (isSuperAdmin) {

            if (creatorMode) {
                return timeOffRequestAdapter.fetchCreatorRequests(
                        request.getFromDate(),
                        request.getToDate(),
                        statusArr,
                        policyArr,
                        loggedUserId
                );
            } else {
                return timeOffRequestAdapter.fetchAllRequests(
                        request.getFromDate(),
                        request.getToDate(),
                        statusArr,
                        policyArr
                );
            }

        } else {

            if (creatorMode) {
                return timeOffRequestAdapter.fetchCreatorRequests(
                        request.getFromDate(),
                        request.getToDate(),
                        statusArr,
                        policyArr,
                        loggedUserId
                );
            }

            return timeOffRequestAdapter.fetchReceiverRequests(
                    request.getFromDate(),
                    request.getToDate(),
                    statusArr,
                    policyArr,
                    loggedUserId
            );
        }
    }

    @Override
    public List<EnumModel> getHourType() {
        List<EnumModel> list = new ArrayList<>();
        for (HourType e : HourType.values()) {
            EnumModel model = new EnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }
}
