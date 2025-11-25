package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffRequestAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public TimeOffRequestServiceImpl(TimeOffRequestAdapter timeOffRequestAdapter, TimeOffPolicyEntityMapper TimeOffPolicyEntityMapper, LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyAdapter timeOffPolicyAdapter) {
        this.timeOffRequestAdapter = timeOffRequestAdapter;
        this.TimeOffPolicyEntityMapper = TimeOffPolicyEntityMapper;
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
    }

    @Override
    public void createRequest(TimeOffRequest request) {
        TimeOffRequestEntity entity = new TimeOffRequestEntity();
        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findPolicyById(request.getPolicyId());
        boolean exists = timeOffRequestAdapter.existsTimeoffRequest(request.getUserId(), request.getPolicyId(), LocalDate.now(zoneId));
        if (exists) {
            throw new IllegalArgumentException("Duplicate requests on the same day are not allowed");
        }
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(policy.getPolicyId(), request.getUserId(), request.getStartDate(), request.getEndDate());
        if (request.getStartDate() == null || request.getEndDate() == null ||  LocalDate.now(zoneId).isAfter(request.getStartDate())){
            throw new IllegalArgumentException("Invalid request format for the selected entitled type");
        }
        double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        Integer requested = 0;
        boolean invalidDayOrHalfDay =
                (policy.getEntitledType() == EntitledType.DAY || policy.getEntitledType() == EntitledType.HALF_DAY)
                        && (request.getStartTime() != null || request.getEndTime() != null || request.getUnitsRequested() == null || request.getHoursRequested() != null || request.getUnitsRequested() != days);
        boolean invalidHour =
                policy.getEntitledType() == EntitledType.HOURS
                        && (request.getUnitsRequested() != null  || days != 1 || request.getHoursRequested() == null ||
                        request.getStartTime() == null || request.getEndTime() == null);
        boolean invalidHalfDay =
                policy.getEntitledType() == EntitledType.HALF_DAY
                        && days != 1;
        if (invalidDayOrHalfDay || invalidHour || invalidHalfDay) {
            throw new IllegalArgumentException("Invalid request format for the selected entitled type");
        }
        if (leaveBalance.getBalanceUnits() == 0.0){
            throw new IllegalArgumentException("Cannot take paid leave");
        }
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        if (policy.getEntitledType() == EntitledType.DAY) {
            requested = request.getUnitsRequested();
            validateDays(days, leaveBalance);
            entity.setUnitsRequested(request.getUnitsRequested());
        }
        else if (policy.getEntitledType() == EntitledType.HALF_DAY) {
            requested = request.getUnitsRequested();
            days = request.getUnitsRequested() * 0.5;
            validateDays(days, leaveBalance);
            entity.setUnitsRequested(request.getUnitsRequested());
        }
        else {
            requested = request.getHoursRequested();
            validateHours(request.getStartTime(), request.getEndTime(), request.getHoursRequested(), leaveBalance);
            entity.setStartTime(request.getStartTime());
            entity.setEndTime(request.getEndTime());
            entity.setHoursRequested(request.getHoursRequested());
        }
        entity.setUserId(request.getUserId());
        entity.setPolicy(policy);
        entity.setStatus(Status.PENDING);
        entity.setReason(request.getReason());
        entity.setRequestDate(LocalDate.now(zoneId));
        TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
        deductLeaveBalance(saved, requested);
        List<UsersRequestMappingEntity> usersMapping =
                Stream.concat(
                        Stream.of(buildMapping(ViewerType.APPROVER, request.getTo(), request.getUserId(), saved.getTimeOffRequestId())),
                        request.getCc().stream().map(viewer -> buildMapping(ViewerType.VIEWER, viewer, request.getUserId(), saved.getTimeOffRequestId()))
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

    private void validateHours( LocalTime startTime, LocalTime endTime, Integer hoursRequested, LeaveBalanceEntity leaveBalance){
        if (startTime.isAfter(endTime)){
            throw new IllegalArgumentException("Invalid duration");
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes % 60 != 0 || (minutes / 60) != hoursRequested ) {
            throw new IllegalArgumentException("Invalid duration");
        }
        double hours = minutes / 60.0;
        if (hours > leaveBalance.getBalanceUnits()) {
            throw new IllegalArgumentException("Insufficient leave balance.");
        }
    }

    private void validateDays(double days, LeaveBalanceEntity leaveBalance){
        if (days > leaveBalance.getBalanceUnits() ) {
            throw new IllegalArgumentException("Insufficient leave balance.");
        }
    }

    @Override
    public void employeeUpdateStatus(EmployeeStatusUpdate model) {
        checkCredentials(model.getPolicyId(), model.getUserId(), model.getRequestDate());
        TimeOffRequestEntity entity = timeOffRequestAdapter.getTimeoffRequest(model.getPolicyId(), model.getUserId(), model.getRequestDate());
        if ( LocalDate.now(zoneId).isAfter(entity.getStartDate()) || (model.getStatus() != null && !handleEmployeeRules(entity.getStatus(), model.getStatus()))) {
            throw new IllegalArgumentException("Update not allowed");
        }
        if (model.getStatus() != null && model.getStatus() == Status.CANCELLED){
            TimeOffPolicyEntity policy = entity.getPolicy();
            Integer requested = policy.getEntitledType() == EntitledType.HOURS ? entity.getHoursRequested() : entity.getUnitsRequested();
            addLeaveBalance(entity, requested);
            entity.setStatus(model.getStatus());
            timeOffRequestAdapter.saveRequest(entity);
            return;
        }
        else {
            if (entity.getPolicy().getEntitledType() == EntitledType.HOURS) {
                if (model.getHoursRequested() > entity.getHoursRequested()) {
                    deductLeaveBalance(entity, model.getHoursRequested() - entity.getHoursRequested());
                } else if (model.getHoursRequested() < entity.getHoursRequested()) {
                    addLeaveBalance(entity, entity.getHoursRequested() - model.getHoursRequested());
                }
            } else {
                if (model.getUnitsRequested() > entity.getUnitsRequested()) {
                    deductLeaveBalance(entity, model.getUnitsRequested() - entity.getUnitsRequested());
                } else if (model.getUnitsRequested() < entity.getUnitsRequested()) {
                    addLeaveBalance(entity, entity.getUnitsRequested() - model.getUnitsRequested());
                }
            }
        }
        if (model.getStatus() != null) {
            entity.setStatus(model.getStatus());
        }
        if (model.getReason() != null) {
            entity.setReason(model.getReason());
        }
        double days =  ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;
        if (model.getStartDate() != null && model.getEndDate() != null){
            days =  ChronoUnit.DAYS.between(model.getStartDate(), model.getEndDate()) + 1;
            entity.setStartDate(model.getStartDate());
            entity.setEndDate(model.getEndDate());
        }
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUserId(), entity.getStartDate(), entity.getEndDate());
        if ((entity.getPolicy().getEntitledType() == EntitledType.DAY || entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY )&&(model.getUnitsRequested() != null && model.getStartDate() != null && model.getEndDate() != null)) {

            if (model.getUnitsRequested() != days){
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            if (entity.getPolicy().getEntitledType() == EntitledType.DAY ){
                validateDays(days, leaveBalance);
            }
            if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) {
                if ( days != 1){
                    throw new IllegalArgumentException("Invalid paid leave request.");
                }
                validateDays(days*0.5, leaveBalance);
            }
        }

        if (entity.getPolicy().getEntitledType() == EntitledType.HOURS && (model.getStartTime() != null && model.getEndTime() != null &&
                model.getHoursRequested()!= null)) {
            if (days != 1){
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            validateHours( model.getStartTime(), model.getEndTime(), model.getHoursRequested(), leaveBalance);
            entity.setStartTime(model.getStartTime());
            entity.setEndTime(model.getEndTime());
        }

        entity.setUnitsRequested(model.getUnitsRequested());
        TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
    }

    private boolean handleEmployeeRules(Status current, Status next) {
        return (current == Status.PENDING || current == Status.APPROVED) && next == Status.CANCELLED;
    }

    private void checkCredentials(String policyId, String userId, LocalDate requestDate){
        if (policyId == null || userId == null || requestDate == null){
            throw new IllegalArgumentException("Missing required fields.");
        }
    }
    @Override
    public void adminUpdateStatus(AdminStatusUpdate model) {
        checkCredentials(model.getPolicyId(), model.getUserId(), model.getRequestDate());
        TimeOffRequestEntity entity = timeOffRequestAdapter.getTimeoffRequest(model.getPolicyId(), model.getUserId(), model.getRequestDate());
        if ( LocalDate.now(zoneId).isAfter(entity.getStartDate())|| (model.getStatus() != null && !handleAdminRules(entity.getStatus(), model.getStatus()))) {
            throw new IllegalArgumentException("Update not allowed. Invalid date or status");
        }
        if(model.getStatus() != null) {
            entity.setStatus(model.getStatus());
            TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
            if (saved.getStatus() == Status.REJECTED) {
                TimeOffPolicyEntity policy = saved.getPolicy();
                Integer requested = policy.getEntitledType() == EntitledType.HOURS ? saved.getHoursRequested() : saved.getUnitsRequested();
                addLeaveBalance(saved, requested);
            }
        }
    }

    private boolean handleAdminRules(Status current, Status next) {
        return switch (current) {
            case PENDING -> next == Status.APPROVED || next == Status.REJECTED;
            case APPROVED -> next == Status.REJECTED;
            default -> false;
        };
    }

    public void deductLeaveBalance(TimeOffRequestEntity entity, Integer requested){

        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUserId(), entity.getStartDate(), entity.getEndDate());
        if(leaveBalance != null && entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY ) {
            log.info("leave balance find");
            leaveBalance.setExpiredUnits(leaveBalance.getExpiredUnits() + requested*0.5);
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() + requested*0.5);
            double balanceUnits = leaveBalance.getBalanceUnits() - requested*0.5;
            leaveBalance.setBalanceUnits(balanceUnits);
            if (entity.getPolicy().getCarryForward() && balanceUnits < leaveBalance.getCarryForwardUnits() ){
                leaveBalance.setCarryForwardUnits(balanceUnits);
            }
        }
        else if (leaveBalance != null &&  (entity.getPolicy().getEntitledType() == EntitledType.HOURS ||
                entity.getPolicy().getEntitledType() == EntitledType.DAY)) {
            leaveBalance.setExpiredUnits(leaveBalance.getExpiredUnits() + requested);
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() + requested);
            double balanceUnits = leaveBalance.getBalanceUnits() - requested;
            leaveBalance.setBalanceUnits(balanceUnits);
            if (entity.getPolicy().getCarryForward() && balanceUnits < leaveBalance.getCarryForwardUnits()) {
                leaveBalance.setCarryForwardUnits(balanceUnits);
            }
        }
        leaveBalanceAdapter.saveLeaveBalance(leaveBalance);
    }

    public void addLeaveBalance(TimeOffRequestEntity entity, Integer requested){
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUserId(), entity.getStartDate(), entity.getEndDate());
        if(leaveBalance != null && entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY ) {
            log.info("leave balance finds");
            leaveBalance.setExpiredUnits(leaveBalance.getExpiredUnits() - requested*0.5);
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() - requested*0.5);
            double balanceUnits = leaveBalance.getBalanceUnits() + requested*0.5;
            leaveBalance.setBalanceUnits(balanceUnits);
            if (entity.getPolicy().getCarryForward() && balanceUnits < leaveBalance.getCarryForwardUnits() ){
                leaveBalance.setCarryForwardUnits(balanceUnits);
            }
        }
        else if (leaveBalance != null &&  (entity.getPolicy().getEntitledType() == EntitledType.HOURS ||
                entity.getPolicy().getEntitledType() == EntitledType.DAY)) {
            leaveBalance.setExpiredUnits(leaveBalance.getExpiredUnits() - requested);
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() - requested);
            double balanceUnits = leaveBalance.getBalanceUnits() + requested;
            leaveBalance.setBalanceUnits(balanceUnits);
            if (entity.getPolicy().getCarryForward() && balanceUnits < leaveBalance.getCarryForwardUnits()) {
                leaveBalance.setCarryForwardUnits(balanceUnits);
            }
        }
        leaveBalanceAdapter.saveLeaveBalance(leaveBalance);
    }

    @Override
    public List<StatusEnumModel> getStatus() {
        List<StatusEnumModel> list=new ArrayList<>();
        for(Compensation e: Compensation.values()){
            StatusEnumModel model= new StatusEnumModel(e.name(),e.getValue());
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
    public List<TimeOffRequestResponseModel> getRequestsByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");}
        List<TimeOffRequestUserModel> list = timeOffRequestAdapter.filterWithUser(fromDate, toDate);
        List<TimeOffRequestResponseModel> response = new ArrayList<>();
        for (TimeOffRequestUserModel item : list) {
            TimeOffRequestResponseModel model = TimeOffPolicyEntityMapper.toModel(item.getRequest());
            model.setUserName(item.getUserName());
            response.add(model);
        }

        return response;
    }
}
