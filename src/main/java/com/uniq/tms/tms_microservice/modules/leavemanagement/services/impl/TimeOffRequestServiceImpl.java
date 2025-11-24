package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffRequestAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeoffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.EntitledType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ViewerType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffRequestService;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

    private final TimeOffRequestAdapter timeOffRequestAdapter;
    private final TimeoffPolicyAdapter timeoffPolicyAdapter;
    private final TimeOffPolicyEntityMapper TimeOffPolicyEntityMapper;

    public TimeOffRequestServiceImpl(TimeOffRequestAdapter timeOffRequestAdapter, TimeoffPolicyAdapter timeoffPolicyAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper) {
        this.timeOffRequestAdapter = timeOffRequestAdapter;
        this.timeoffPolicyAdapter = timeoffPolicyAdapter;
        TimeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
    }

    @Override
    public void createRequest(TimeOffRequest request) {
        TimeOffRequestEntity entity = new TimeOffRequestEntity();
        TimeOffPolicyEntity policy = timeoffPolicyAdapter.findPolicyById(request.getPolicyId());
        LeaveBalanceEntity leaveBalance = timeOffRequestAdapter.findLeaveBalance(request.getPolicyId(),request.getUserId());
        boolean exists = timeOffRequestAdapter.existsTimeoffRequest(request.getUserId(), request.getPolicyId());
        if (exists) {
            throw new IllegalArgumentException("You cannot create another request for the same policy at this time.");
        }
        double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        boolean invalidDayOrHalfDay =
                (policy.getEntitledType() == EntitledType.DAY || policy.getEntitledType() == EntitledType.HALF_DAY)
                        && (request.getStartTime() != null || request.getEndTime() != null || request.getUnitsRequested() == null || request.getHoursRequested() != null || request.getUnitsRequested() != days);
        boolean invalidHour =
                policy.getEntitledType() == EntitledType.HOURS
                        && (request.getUnitsRequested() != null  || days != 1 || request.getHoursRequested() == null);
        boolean invalidHalfDay =
                policy.getEntitledType() == EntitledType.HALF_DAY
                        && days != 1;
        if (invalidDayOrHalfDay || invalidHour || invalidHalfDay) {
            throw new IllegalArgumentException("Invalid request format for the selected entitled type");
        }

        if (policy.getEntitledType() == EntitledType.DAY) {

            if (leaveBalance.getBalanceUnits() == 0.0 ||
                    days > leaveBalance.getBalanceUnits()) {
                throw new IllegalArgumentException("Cannot take paid leave");
            }
            entity.setStartDate(request.getStartDate());
            entity.setEndDate(request.getEndDate());
            entity.setUnitsRequested(request.getUnitsRequested());
        }
        else if (policy.getEntitledType() == EntitledType.HALF_DAY) {
            days = request.getUnitsRequested() * 0.5;
            if (leaveBalance.getBalanceUnits() == 0.0 ||
                    days > leaveBalance.getBalanceUnits()) {
                throw new IllegalArgumentException("Cannot take paid leave");
            }
            entity.setStartDate(request.getStartDate());
            entity.setEndDate(request.getEndDate());
            entity.setUnitsRequested(request.getUnitsRequested());
        }
        else {

            long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
            if (minutes % 60 != 0 || (minutes / 60) != request.getHoursRequested()) {
                throw new IllegalArgumentException("Invalid duration");
            }
            double hours = minutes / 60.0;
            if (leaveBalance.getBalanceUnits() == 0.0 ||
                    hours > leaveBalance.getBalanceUnits()) {
                throw new IllegalArgumentException("Cannot take permission");
            }
            entity.setStartTime(request.getStartTime());
            entity.setEndTime(request.getEndTime());
            entity.setHoursRequested(request.getHoursRequested());
        }
        entity.setUserId(request.getUserId());
        entity.setPolicy(policy);
        entity.setStatus(Status.PENDING);
        entity.setReason(request.getReason());
        entity.setRequestDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
        entity.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        entity.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
        List<UsersRequestMappingEntity> usersMapping =
                Stream.concat(
                        Stream.of(buildMapping(ViewerType.APPROVER, request.getTo(), request.getUserId(), saved.getTimeoffRequestId())),
                        request.getCc().stream().map(viewer -> buildMapping(ViewerType.VIEWER, viewer, request.getUserId(), saved.getTimeoffRequestId()))
                ).toList();
        List<UsersRequestMappingEntity> entities = timeOffRequestAdapter.saveUsersRequestMapping(usersMapping);
    }

    private UsersRequestMappingEntity buildMapping(ViewerType type, String viewerId, String requesterId, Long requestId) {
        UsersRequestMappingEntity m = new UsersRequestMappingEntity();
        m.setType(type);
        m.setViewerId(viewerId);
        m.setRequesterId(requesterId);
        m.setTimeoffRequestId(requestId);
        return m;
    }

    @Override
    public void employeeUpdateStatus(EmployeeStatusUpdate model) {
        TimeOffRequestEntity entity = timeOffRequestAdapter.findByUserIdAndRequestDate(model.getUserId(), model.getRequestDate());
        LeaveBalanceEntity leaveBalance = timeOffRequestAdapter.findLeaveBalance(entity.getPolicy().getPolicyId(), entity.getUserId());
        if (entity == null) {
            throw new IllegalArgumentException("Request not found");
        }
        if (LocalDate.now().isAfter(entity.getStartDate()) || handleEmployeeRules(entity.getStatus(), model.getStatus())) {
            throw new IllegalArgumentException("Update not allowed: leave started or invalid status.");
        }
        if (model.getStatus() != null) {
            entity.setStatus(model.getStatus());
        }
        if (model.getReason() != null) {
            entity.setReason(model.getReason());
        }
        if ((entity.getPolicy().getEntitledType() == EntitledType.DAY || entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY)
                && model.getStartAndEndDates() != null && model.getStartAndEndDates().contains("to")) {

            String[] dates = model.getStartAndEndDates().split("to");
            LocalDate start = LocalDate.parse(dates[0].trim());
            LocalDate end = LocalDate.parse(dates[1].trim());
            int days = (int) ChronoUnit.DAYS.between(start, end) + 1;
            if (entity.getPolicy().getEntitledType() == EntitledType.DAY && (entity.getUnitsRequested() > leaveBalance.getBalanceUnits()) || (days != leaveBalance.getBalanceUnits())){
                throw new IllegalArgumentException("Cannot take paid leave");
            }
            if(entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY && (entity.getUnitsRequested()*0.5 > leaveBalance.getBalanceUnits() || days*0.5 != leaveBalance.getBalanceUnits())){
                throw new IllegalArgumentException("Cannot take paid leave");
            }
            entity.setStartDate(start);
            entity.setEndDate(end);
        }
        if (entity.getPolicy().getEntitledType() == EntitledType.HOURS && model.getStartAndEndTimes() != null && model.getStartAndEndTimes().contains("to")) {
            String[] times = model.getStartAndEndTimes().split("to");
            LocalTime start = LocalTime.parse(times[0].trim());
            LocalTime end = LocalTime.parse(times[1].trim());
            entity.setStartTime(start);
            entity.setEndTime(end);
            int minutes = (int) ChronoUnit.MINUTES.between(start, end);
            if (minutes % 60 != 0){
                throw new IllegalArgumentException("Invalid time format");
            }
            double hours = minutes / 60.0;
            if (entity.getHoursRequested() > leaveBalance.getBalanceUnits() || hours != entity.getHoursRequested()){
                throw new IllegalArgumentException("Cannot take permission");
            }
        }
        if (model.getUnitsRequested() != null) {
            entity.setUnitsRequested(model.getUnitsRequested());
        }
        if (model.getHoursRequested() != null) {
            entity.setHoursRequested(model.getHoursRequested());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        timeOffRequestAdapter.saveRequest(entity);
    }

    private boolean handleEmployeeRules(Status current, Status next) {
        return (current == Status.PENDING || current == Status.APPROVED) && next == Status.CANCELLED;
    }

    @Override
    public void adminUpdateStatus(AdminStatusUpdate model) {
        TimeOffRequestEntity entity = timeOffRequestAdapter.findByUserIdAndRequestDate(model.getUserId(), model.getRequestDate());
        if (LocalDate.now().isAfter(entity.getStartDate()) || handleEmployeeRules(entity.getStatus(), model.getStatus())) {
            throw new IllegalArgumentException("Update not allowed: leave started or invalid status.");
        }
        if (model.getStatus() != null) {
            entity.setStatus(model.getStatus());
        }
        timeOffRequestAdapter.saveRequest(entity);
    }

    private boolean handleAdminRules(Status current, Status next) {
        return switch (current) {
            case PENDING -> next == Status.APPROVED || next == Status.REJECTED;
            case APPROVED -> next == Status.REJECTED;
            default -> false;
        };
    }

    public void updateLeaveBalance(){
        List<TimeOffRequestEntity> entities = timeOffRequestAdapter.findStartByDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
        List<LeaveBalanceEntity> leaveBalanceEntities = new ArrayList<>();
        for(TimeOffRequestEntity entity : entities){
            LeaveBalanceEntity leaveBalance = timeOffRequestAdapter.findLeaveBalance(entity.getPolicy().getPolicyId(), entity.getUserId());
            if(leaveBalance != null && leaveBalance.getPolicy().getEntitledType() == EntitledType.HALF_DAY ) {
                leaveBalance.setExpiredUnits(leaveBalance.getExpiredUnits()-entity.getUnitsRequested()*0.5);
                leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits()+entity.getUnitsRequested()*0.5);
                leaveBalance.setBalanceUnits(leaveBalance.getBalanceUnits()+entity.getUnitsRequested()*0.5);
            }
            else if (leaveBalance != null &&  leaveBalance.getPolicy().getEntitledType() == EntitledType.HOURS ||
                    Objects.requireNonNull(leaveBalance).getPolicy().getEntitledType() == EntitledType.DAY){
                leaveBalance.setExpiredUnits(leaveBalance.getExpiredUnits()-entity.getUnitsRequested());
                leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits()+entity.getUnitsRequested());
                leaveBalance.setBalanceUnits(leaveBalance.getBalanceUnits()+entity.getUnitsRequested());
            }
            leaveBalanceEntities.add(leaveBalance);
        }
        timeOffRequestAdapter.saveAllLeaveBalance(leaveBalanceEntities);
    }

    @Override
    public List<TimeOffRequestResponseModel> getRequestsByDateRange(RequestFilterModel model) {

        if (model.getFromDate() == null || model.getToDate() == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }

        List<TimeOffRequestUserModel> list =
                timeOffRequestAdapter.filterWithUser(model.getFromDate(), model.getToDate());

        List<TimeOffRequestResponseModel> response = new ArrayList<>();

        for (TimeOffRequestUserModel item : list) {
            TimeOffRequestResponseModel dto =
                    TimeOffPolicyEntityMapper.toModel(item.getRequest());

            dto.setUserName(item.getUserName());

            response.add(dto);
        }

        return response;
    }
}
