package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.opencsv.CSVWriter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffRequestAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.UserPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffExportDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffExportRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.ViewerDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.TimeOffExportView;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffRequestService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.TimesheetStatusEnum;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.MemberType;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.RoleName;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FixedWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FlexibleWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.DayOfWeekEnum;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.WorkScheduleTypeEnum;
import com.uniq.tms.tms_microservice.shared.dto.EnumModel;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.shared.util.DateTimeUtil;
import com.uniq.tms.tms_microservice.shared.util.ExportStatusTracker;
import com.uniq.tms.tms_microservice.shared.util.ReportStyleUtil;
import jakarta.annotation.Nullable;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bouncycastle.oer.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;

@Service
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

    private static final Logger log = LoggerFactory.getLogger(TimeOffRequestServiceImpl.class);

    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    private final TimeOffRequestAdapter timeOffRequestAdapter;
    private final TimeOffPolicyEntityMapper TimeOffPolicyEntityMapper;
    private final TimeOffPolicyDtoMapper timeOffPolicyDtoMapper;
    private final LeaveBalanceAdapter leaveBalanceAdapter;
    private final TimeOffPolicyAdapter timeOffPolicyAdapter;
    private final UserPolicyAdapter userPolicyAdapter;
    private final AuthHelper authHelper;
    private final CacheKeyUtil cacheKeyUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReportStyleUtil reportStyleUtil;
    private final UserAdapter userAdapter;
    private final TimesheetAdapter timesheetAdapter;
    private final TimesheetService timesheetService;
    private final ExportStatusTracker exportStatusTracker;

    public TimeOffRequestServiceImpl(TimeOffRequestAdapter timeOffRequestAdapter, TimeOffPolicyEntityMapper TimeOffPolicyEntityMapper, TimeOffPolicyDtoMapper timeOffPolicyDtoMapper,
                                     LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyAdapter timeOffPolicyAdapter, UserPolicyAdapter userPolicyAdapter,
                                     AuthHelper authHelper, CacheKeyUtil cacheKeyUtil, @Nullable RedisTemplate<String, Object> redisTemplate, ReportStyleUtil reportStyleUtil, UserAdapter userAdapter, TimesheetAdapter timesheetAdapter, TimesheetService timesheetService, ExportStatusTracker exportStatusTracker) {
        this.timeOffRequestAdapter = timeOffRequestAdapter;
        this.TimeOffPolicyEntityMapper = TimeOffPolicyEntityMapper;
        this.timeOffPolicyDtoMapper = timeOffPolicyDtoMapper;
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
        this.userPolicyAdapter = userPolicyAdapter;
        this.authHelper = authHelper;
        this.cacheKeyUtil = cacheKeyUtil;
        this.redisTemplate = redisTemplate;
        this.reportStyleUtil = reportStyleUtil;
        this.userAdapter = userAdapter;
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetService = timesheetService;
        this.exportStatusTracker = exportStatusTracker;
    }

    @Value("${csv.request.download.dir}")
    private String downloadDir;

    @Override
    public void createRequest(TimeOffRequest request) {
        boolean validPolicy = timeOffPolicyAdapter.existsValidPolicy(request.getPolicyId(), request.getStartDate(), request.getEndDate());
        boolean validUserPolicy = userPolicyAdapter.isUserPolicyActive(request.getPolicyId(), request.getUserId(), request.getStartDate(), request.getEndDate());
        if (!validPolicy) {
            throw new IllegalStateException("Invalid policy for the given date.");
        }
        if (!validUserPolicy) {
            throw new IllegalStateException("Invalid user policy for the given date.");
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
            throw new IllegalArgumentException("Request already exists within this date range.");
        }
        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findPolicyById(request.getPolicyId());
        UserEntity user = userAdapter.getUserById(request.getUserId());
        if (user.getRequestApproverId() == null || user.getUserId().equals(user.getRequestApproverId())){
            throw new IllegalArgumentException("requestId is missing or invalid.");
        }
        WorkScheduleEntity workSchedule = user.getWorkSchedule();
        double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (policy.getEntitledType() == EntitledType.DAY && days != request.getUnitsRequested()) {
            throw new IllegalArgumentException("Invalid request format for the selected entitled type");
        }
        TimeOffRequestEntity entity = new TimeOffRequestEntity();
        Integer requested = 0;
        if (policy.getCompensation() == Compensation.PAID) {
            LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(policy.getPolicyId(), request.getUserId(), request.getStartDate(), request.getEndDate());
            if (leaveBalance.getBalanceUnits() == 0.0) {
                throw new IllegalArgumentException("Cannot take paid leave");
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
        entity.setStatus(Status.PENDING);
        entity.setReason(request.getReason());
        entity.setRequestDate(LocalDate.now(zoneId));
        List<Long> groupIds = user.getUserGroups().stream()
                .map(g -> g.getGroup().getGroupId())
                .toList();
        Set<String> viewers = userAdapter.getAllSupervisorIds(groupIds, user.getUserId(), MemberType.SUPERVISOR.getValue());
        log.info("viewers size{}",viewers.size());
        String superAdminId =userAdapter.findSuperAdminByOrgId(authHelper.getOrgId())
                .map(UserEntity::getUserId)
                .orElseThrow(() -> new IllegalArgumentException("Super admin not found"));
        viewers.remove(user.getRequestApproverId());
        viewers.remove(superAdminId);

        TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
        deductLeaveBalance(saved, requested);

        List<UsersRequestMappingEntity> usersMapping =
                Stream.concat(
                        Stream.of(buildMapping(ViewerType.APPROVER, superAdminId, request.getUserId(), saved.getTimeOffRequestId()),
                        (buildMapping(ViewerType.APPROVER, user.getRequestApproverId(), request.getUserId(), saved.getTimeOffRequestId()))),
                        viewers.stream().map(viewer -> buildMapping(ViewerType.VIEWER, viewer, request.getUserId(), saved.getTimeOffRequestId()))
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

    private double validateHours(WorkScheduleEntity workSchedule, LocalTime startTime, LocalTime endTime, Integer hoursRequested, LeaveBalanceEntity leaveBalance, LocalDate date) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        DayOfWeekEnum day = DayOfWeekEnum.valueOf(date.getDayOfWeek().name());
        if (workSchedule.getType().getType() == WorkScheduleTypeEnum.FIXED  ){
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
                throw new IllegalArgumentException("Start and End times must be within the allowed time range");
            }
        }
        else {
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

    private void setTimeForHalf(WorkScheduleEntity workSchedule, LocalDate startDate, HourType leaveHalf, TimeOffRequestEntity entity){
        DayOfWeekEnum day = DayOfWeekEnum.valueOf(startDate.getDayOfWeek().name());
        if (workSchedule.getType().getType() == WorkScheduleTypeEnum.FIXED){
            FixedWorkScheduleEntity fixedEntity =
                    timesheetAdapter.findByWorkScheduleIdAndDay(workSchedule.getScheduleId(), day);
            double half = fixedEntity.getDuration()/2;
            int hours = (int) half;
            int minutes = (int) ((half - hours) * 60);
            LocalTime halfTime = fixedEntity.getStartTime().toLocalTime().plusHours(hours).plusMinutes(minutes);
            if (leaveHalf == HourType.FIRST_HALF){
                entity.setStartTime(fixedEntity.getStartTime().toLocalTime());
                entity.setEndTime(halfTime);
            }
            else {
                entity.setStartTime(halfTime);
                entity.setEndTime(fixedEntity.getEndTime().toLocalTime());
            }
        }
    }

    @Override
    @Transactional
    public void employeeUpdateStatus(EmployeeStatusUpdate model) {
        checkCredentials(model.getPolicyId(), model.getRequestDate());
        if (model.getStartDate() != null) {
            boolean validUserPolicy = userPolicyAdapter.isUserPolicyActive(model.getPolicyId(), model.getUserId(), model.getStartDate(), model.getEndDate());
            if (!validUserPolicy) {
                throw new IllegalStateException("Invalid policy for the given date.");
            }
        }

        TimeOffRequestEntity entity = timeOffRequestAdapter.getTimeoffRequest(model.getPolicyId(), model.getUserId(), model.getRequestDate());
        WorkScheduleEntity workSchedule = entity.getUser().getWorkSchedule();
        if (LocalDate.now(zoneId).isAfter(entity.getStartDate()) || (model.getStatus() != null && !handleEmployeeRules(entity.getStatus(), model.getStatus()))) {
            throw new IllegalArgumentException("Update not allowed");
        }
        if (model.getStatus() != null && model.getStatus() == Status.CANCELLED) {
            if (entity.getPolicy().getCompensation() == Compensation.PAID) {
                Integer requested = entity.getUnitsRequested();
                addLeaveBalance(entity, requested);
            }
            entity.setStatus(model.getStatus());
            timeOffRequestAdapter.saveRequest(entity);
            timesheetService.deleteTimesheet(model.getUserId(),model.getStartDate());
            return;
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
            timeOffRequestAdapter.saveRequest(entity);
            return;
        }
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUser().getUserId(), entity.getStartDate(), entity.getEndDate());
        if ((entity.getPolicy().getEntitledType() == EntitledType.DAY || entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) && (model.getUnitsRequested() != null && model.getStartDate() != null && model.getEndDate() != null)) {

            if (model.getUnitsRequested() != days) {
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            if (entity.getPolicy().getEntitledType() == EntitledType.DAY) {
                if (model.getUnitsRequested() > entity.getUnitsRequested()) {
                    days = model.getUnitsRequested() - entity.getUnitsRequested();
                    validateDays(days, leaveBalance.getBalanceUnits());
                }
            }
            if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY ) {
                if (days != 1) {
                    throw new IllegalArgumentException("Invalid paid leave request.");
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

    private void checkCredentials(String policyId, LocalDate requestDate){
        if (policyId == null || requestDate == null){
            throw new IllegalArgumentException("Missing required fields");
        }
    }

    private boolean handleEmployeeRules(Status current, Status next) {
        return ((current == Status.PENDING || current == Status.APPROVED) && (next == Status.CANCELLED ) ||
        (current == Status.PENDING && next == Status.PENDING));
    }

    @Override
    @Transactional
    public void adminUpdateStatus(AdminStatusUpdate model) {
        checkCredentials(model.getPolicyId(), model.getRequestDate());
        TimeOffRequestEntity entity = timeOffRequestAdapter.getTimeoffRequest(model.getPolicyId(), model.getUserId(), model.getRequestDate());
        boolean dateInvalid = LocalDate.now(zoneId).isAfter(entity.getStartDate());
        boolean statusInvalid = (model.getStatus() != null && !handleAdminRules(entity.getStatus(), model.getStatus()));
        if (dateInvalid || statusInvalid) {
            log.warn("Update not allowed. Date invalid = {}, Status invalid = {}", dateInvalid, statusInvalid);
            throw new IllegalArgumentException("Update not allowed. Invalid date or status");
        }
        if (model.getStatus() != null) {
            log.info("Updating status from {} → {}", entity.getStatus(), model.getStatus());
            entity.setStatus(model.getStatus());
            TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
            log.info("Status updated successfully. Saved status: {}", saved.getStatus());
            if (saved.getStatus().equals(Status.APPROVED)) {
                if(entity.getPolicy().getCompensation().equals(Compensation.PAID)) {
                    log.info("Policy is PAID. Applying entitled type: {}", entity.getPolicy().getEntitledType());
                    switch (entity.getPolicy().getEntitledType()) {

                        case DAY -> {
                            log.info("Applying PAID_LEAVE to timesheet");
                            timesheetService.createTimesheet(
                                    TimesheetStatusEnum.PAID_LEAVE,
                                    model.getUserId(),
                                    entity.getStartDate()
                            );
                        }
                        case HALF_DAY -> {
                            log.info("Applying HALF_DAY leave to timesheet");
                            timesheetService.createTimesheet(
                                    TimesheetStatusEnum.HALF_DAY,
                                    model.getUserId(),
                                    entity.getStartDate()
                            );
                        }
                        case HOURS -> {
                            log.info("Applying PERMISSION leave (Hours type)");
                            timesheetService.createTimesheet(
                                    TimesheetStatusEnum.PERMISSION,
                                    model.getUserId(),
                                    entity.getStartDate()
                            );
                        }
                    }
                }else {
                    log.info("Policy is UNPAID → Marking ABSENT in timesheet");
                    timesheetService.createTimesheet(
                            TimesheetStatusEnum.UNPAID_LEAVE,
                            model.getUserId(),
                            entity.getStartDate()
                    );
                }
            } else if (saved.getStatus() == Status.REJECTED) {
                log.info("Request REJECTED → Restoring leave balance and deleting timesheet entry");
                Integer requested = saved.getUnitsRequested();
                addLeaveBalance(saved, requested);
                log.info("Leave balance restored for {} units", requested);
                timesheetService.deleteTimesheet(
                        model.getUserId(),
                        entity.getStartDate()
                );
                log.info("Timesheet entry deleted for rejected request");
            }
        }
        log.info("adminUpdateStatus completed successfully for User: {}, Policy: {}",
                model.getUserId(), model.getPolicyId());
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

        List<TimeOffExportView> rows = fetchExportRows(request, loggedUserId);

        Map<String, TimeOffExportModel> grouped = new LinkedHashMap<>();

        for (TimeOffExportView row : rows) {

            String key = row.getCreatorId()
                    + "|" + row.getPolicyId()
                    + "|" + row.getRequestedDate()
                    + "|" + row.getLeaveStartDate()
                    + "|" + row.getLeaveEndDate();

            TimeOffExportModel model = grouped.get(key);

            if (model == null) {
                model = new TimeOffExportModel();
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
                model.setViewers(new ArrayList<>());
                model.setApprover(new ArrayList<>());

                grouped.put(key, model);
            }

            ViewerType viewerType = null;
            if (row.getViewerType() != null) {
                viewerType = ViewerType.valueOf(row.getViewerType().toUpperCase());
            }

            if (viewerType == ViewerType.VIEWER) {
                ViewerModel v = new ViewerModel();
                v.setUserId(row.getViewerId());
                v.setUserName(row.getViewerName());
                v.setViewerType(ViewerType.VIEWER.getValue());
                model.getViewers().add(v);
            }

            if (viewerType == ViewerType.APPROVER) {
                ViewerModel a = new ViewerModel();
                a.setUserId(row.getViewerId());
                a.setUserName(row.getViewerName());
                a.setViewerType(ViewerType.APPROVER.getValue());
                model.getApprover().add(a);
            }
        }

        return new ArrayList<>(grouped.values());
    }

    public List<TimeOffExportView> fetchExportRows(TimeOffExportRequest request, String loggedUserId) {

        String[] statusArr = request.getStatus() == null ? new String[0] : request.getStatus().toArray(new String[0]);
        String[] policyArr = request.getPolicyIds() == null ? new String[0] : request.getPolicyIds().toArray(new String[0]);

        String loggedInUserId = authHelper.getUserId();
        UserEntity user = userAdapter.findById(loggedInUserId).orElseThrow();

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
    @Transactional
    public String startExporting(TimeOffExportRequestDto request, String schema, String orgId) {
        File folder = new File(downloadDir);
        if (!folder.exists()) folder.mkdirs();
        TimeOffExportRequest model = timeOffPolicyDtoMapper.toModel(request);
        String format = request.getFormat() == null ? "xlsx" : request.getFormat().toLowerCase();
        String baseName = "TimeOffRequest_" + request.getFromDate();
        String extension = "." + format;
        String finalName = baseName + extension;
        File file = new File(downloadDir + finalName);
        int count = 1;
        while (file.exists()) {
            finalName = baseName + "(" + count + ")" + extension;
            file = new File(downloadDir + finalName);
            count++;
        }
        String exportKey = cacheKeyUtil.getExport(schema, orgId, finalName);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(exportKey, ReportType.PENDING, Duration.ofHours(12));
        } else {
            exportStatusTracker.writeStatus(file, ReportType.PENDING.getValues());
        }
        generateReportAsync(file, exportKey, model);
        return finalName;
    }

    @Async
    public void generateReportAsync(File file, String exportKey, TimeOffExportRequest request) {
        try {
            boolean redisAvailable = redisTemplate != null;

            if (redisAvailable) {
                redisTemplate.opsForValue().set(exportKey, ReportType.PROCESSING);
            } else {
                exportStatusTracker.writeStatus(file, ReportType.PROCESSING.getValues());
            }

            String loggedInUser = authHelper.getUserId();
            List<TimeOffExportView> exportData = fetchExportRows(request, loggedInUser);

            if ("csv".equalsIgnoreCase(request.getFormat())) {
                generateCsv(exportData, file);
            } else {
                generateXlsx(exportData, file);
            }

            if (redisAvailable) {
                redisTemplate.opsForValue().set(exportKey, ReportType.COMPLETED);
            } else {
                exportStatusTracker.writeStatus(file, ReportType.COMPLETED.getValues());
            }
        } catch (Exception e) {
            log.error("Timeoff request failed", e);
            try {
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        log.warn("Failed to delete file after export failure: {}", file.getAbsolutePath());
                    }
                }
            } catch (Exception deleteEx) {
                log.error("Error deleting failed export file: {}", deleteEx.getMessage());
            }
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(exportKey, ReportType.FAILED.getValues());
            } else {
                exportStatusTracker.writeStatus(file, ReportType.FAILED.getValues());
            }
        }
    }

    private void generateCsv(List<TimeOffExportView> data, File file) throws Exception {
        file.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8);
             CSVWriter csv = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            ClassPathResource resource = new ClassPathResource("templates/text/timeoff_request_csv_header.txt");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String headerLine = br.readLine();
                if (headerLine != null) {
                    csv.writeNext(headerLine.split(","));
                }
            }
            for (TimeOffExportView v : data) {
                csv.writeNext(new String[]{
                        v.getCreatorId(),
                        v.getCreatorName(),
                        v.getPolicyName(),
                        String.valueOf(v.getLeaveStartDate()),
                        String.valueOf(v.getLeaveEndDate()),
                        v.getLeaveType(),
                        v.getStatus(),
                        v.getViewerType()});
            }
            csv.flush();
        }
    }

    private void generateXlsx(List<TimeOffExportView> data, File file) throws Exception {
        file.getParentFile().mkdirs();
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("TimeOff Report");
            Resource resource =
                    new ClassPathResource("templates/text/timeoff_request_excel_header.txt");
            List<String> headerLines;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                headerLines = reader.lines().toList();
            }
            String[] headers = headerLines.getFirst().split("\\|");
            CellStyle headerStyle = reportStyleUtil.createHeaderCellStyle(workbook);
            CellStyle dataStyle = reportStyleUtil.createDataCellStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                reportStyleUtil.createStyledCell(headerRow, i, " " + headers[i] + " ", headerStyle);
            }
            int rowIdx = 1;
            for (TimeOffExportView v : data) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                reportStyleUtil.createStyledCell(row, col++, v.getCreatorId(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, v.getCreatorName(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, v.getPolicyName(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(v.getLeaveStartDate()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(v.getLeaveEndDate()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, v.getLeaveType(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, v.getStatus(), dataStyle);
                if(Objects.equals(v.getViewerType(), ViewerType.APPROVER.getValue())){
                    reportStyleUtil.createStyledCell(row, col++, v.getViewerName(), dataStyle);
                }
                reportStyleUtil.createStyledCell(row, col, v.getViewerName(), dataStyle);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(fos);
        }
    }

    @Override
    public String exportStatus(String exportId, String schema, String orgId) {
        String exportKey = cacheKeyUtil.getExport(schema, orgId, exportId);
        if (redisTemplate != null) {
            Object val = redisTemplate.opsForValue().get(exportKey);
            return (val == null ? "NOT_FOUND" : val.toString());
        }
        File file = new File(downloadDir + exportId);
        String status = exportStatusTracker.readStatus(file);
        return status == null ? "NOT_FOUND" : status;
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
