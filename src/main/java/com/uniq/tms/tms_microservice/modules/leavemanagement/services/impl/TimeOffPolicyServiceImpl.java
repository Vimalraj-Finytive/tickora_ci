package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeoffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.UserPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffPolicyService;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

@Service
public class TimeOffPolicyServiceImpl implements TimeOffPolicyService {

    private static final Logger log = LoggerFactory.getLogger(TimeOffPolicyServiceImpl.class);

    private final TimeoffPolicyAdapter timeoffPolicyAdapter;
    private final IdGenerationService idGenerationService;
    private final UserAdapter userAdapter;
    private final TimeOffPolicyEntityMapper timeOffPolicyEntityMapper;
    private final UserPolicyAdapter userPolicyAdapter;
    private final LeaveBalanceAdapter leaveBalanceAdapter;

    public TimeOffPolicyServiceImpl(TimeoffPolicyAdapter timeoffPolicyAdapter, IdGenerationService idGenerationService, UserAdapter userAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper,  UserPolicyAdapter userPolicyAdapter, LeaveBalanceAdapter leaveBalanceAdapter) {
        this.timeoffPolicyAdapter = timeoffPolicyAdapter;
        this.idGenerationService = idGenerationService;
        this.userAdapter = userAdapter;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
        this.userPolicyAdapter = userPolicyAdapter;
        this.leaveBalanceAdapter = leaveBalanceAdapter;
    }

    @Override
    @Transactional
    public TimeOffPolicyResponseModel createPolicy(TimeOffPolicyRequestModel request) {

        if (request.getCompensation() == Compensation.UNPAID) {
            if (request.getEntitledType() != null ||
                    request.getEntitledUnits() != null ||
                    request.getEntitledHours() != null ||
                    Boolean.TRUE.equals(request.getCarryForward()) ||
                    request.getMaxCarryForwardUnits() != null) {
                throw new IllegalArgumentException(
                        "Entitlement and carry-forward fields are not allowed for UNPAID compensation."
                );
            }
        }

        if (request.getEntitledUnits() != null && request.getEntitledHours() != null) {
            throw new IllegalArgumentException("You cannot provide both entitledUnits and entitledHours together");
        }

        if ((request.getEntitledType() == EntitledType.DAY || request.getEntitledType() == EntitledType.HALF_DAY) && request.getEntitledUnits() == null) {
            throw new IllegalArgumentException("Enter entitledUnits for DAY or HALF_DAY");
        }

        if (request.getEntitledType() == EntitledType.HOURS && request.getEntitledHours() == null)
            throw new IllegalArgumentException("Enter entitledHours for HOURS");

        String policyId = idGenerationService.generateNextTimeOffPolicyId();

        TimeoffPolicyEntity policy = timeOffPolicyEntityMapper.toEntity(request);
        policy.setPolicyId(policyId);

        if (request.getEntitledType() == EntitledType.DAY || request.getEntitledType() == EntitledType.HALF_DAY) {

            policy.setEntitledUnits(
                    request.getAccrualType() == AccrualType.MONTHLY?
                            request.getEntitledUnits() * 12 :
                            request.getEntitledUnits()
            );

            policy.setEntitledHours(null);

        } else {
            policy.setEntitledUnits(null);
            policy.setEntitledHours(request.getEntitledHours());
        }

        policy.setAccrualStartDate(LocalDate.now());
        policy.setResetFrequency(policy.getAccrualType());
        policy.setActive(true);
        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());

        policy = timeoffPolicyAdapter.savePolicy(policy);

        if (request.getCompensation() == Compensation.UNPAID)
            return timeOffPolicyEntityMapper.toResponseModel(policy);

        Set<String> finalUserSet = getFinalUserSet(request.getUserIds(), request.getGroupIds());

        if (finalUserSet.isEmpty())
            return timeOffPolicyEntityMapper.toResponseModel(policy);

        List<String> finalUsers = new ArrayList<>(finalUserSet);

        List<UserPolicyEntity> userPolicies = new ArrayList<>();
        List<LeaveBalanceEntity> leaveBalances = new ArrayList<>();

        LocalDate validFrom = request.getUserValidFrom() != null
                ? request.getUserValidFrom()
                : policy.getValidityStartDate();

        LocalDate validTo = request.getUserValidTo() != null
                ? request.getUserValidTo()
                : policy.getValidityEndDate();

        double totalUnits = calculateTotalUnits(policy, request.getEntitledType());

        for (String userId : finalUsers) {

            UserEntity userEntity = userAdapter.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User ID " + userId + " not found."));
            userPolicies.add(buildUserPolicy(policy, userEntity, validFrom, validTo));
            leaveBalances.add(buildLeaveBalance(policy, userId, validFrom, validTo, totalUnits));
        }

        userPolicyAdapter.saveUserPolicies(userPolicies);
        leaveBalanceAdapter.saveLeaveBalances(leaveBalances);

        return timeOffPolicyEntityMapper.toResponseModel(policy);
    }


    @Override
    public EntitledTypeDropdownModel getDropDowns() {

        List<Map<String, Object>> entitledTypes = Arrays.stream(EntitledType.values())
                .map(type -> Map.<String, Object>of(
                        "key", type.name(),
                        "value", type.getValue()
                ))
                .toList();

        return new EntitledTypeDropdownModel(entitledTypes);
    }

    @Override
    @Transactional
    public void editPolicy(TimeOffPolicyEditRequestModel request) {

        TimeoffPolicyEntity policy = timeoffPolicyAdapter.findByPolicyId(request.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("Invalid Policy ID");
        }

        boolean entitlementChanged = false;

        if (request.getPolicyName() != null && !request.getPolicyName().trim().isEmpty()) {
            policy.setPolicyName(request.getPolicyName().trim());
        }

        if (request.getEntitledUnits() != null) {
            policy.setEntitledUnits(request.getEntitledUnits());
            policy.setEntitledHours(null);
            entitlementChanged = true;
        }

        if (request.getEntitledHours() != null) {
            policy.setEntitledHours(request.getEntitledHours());
            policy.setEntitledUnits(null);
            entitlementChanged = true;
        }

        if (request.getCarryForward() != null) {
            policy.setCarryForward(request.getCarryForward());

            if (request.getCarryForward()) {
                if (request.getMaxCarryForwardUnits() == null) {
                    throw new IllegalArgumentException(
                            "MaxCarryForwardUnits is required when carryForward is TRUE"
                    );
                }
                policy.setMaxCarryForwardUnits(request.getMaxCarryForwardUnits());
            } else {
                policy.setMaxCarryForwardUnits(0);
            }
        }

        if (request.getEntitledUnits() != null && request.getEntitledHours() != null) {
            throw new IllegalArgumentException("You cannot provide both entitledUnits and entitledHours together");
        }

        policy.setUpdatedAt(LocalDateTime.now());
        timeoffPolicyAdapter.savePolicy(policy);

        if (!entitlementChanged)
            return;

        List<UserPolicyEntity> assignedUsers =
                userPolicyAdapter.findUserPoliciesByPolicyId(policy.getPolicyId());

        if (assignedUsers.isEmpty())
            return;

        double totalUnits = calculateTotalUnits(policy, policy.getEntitledType());

        List<LeaveBalanceEntity> leaveBalances =
                leaveBalanceAdapter.findLeaveBalancesByPolicyId(policy.getPolicyId());

        for (LeaveBalanceEntity lb : leaveBalances) {

            lb.setTotalUnits(totalUnits);

            double taken = lb.getLeaveTakenUnits() != null
                    ? lb.getLeaveTakenUnits()
                    : 0.0;

            if (taken == 0.0) {
                lb.setBalanceUnits(totalUnits);
            } else {
                Double newBalance = totalUnits - taken;

                if (newBalance < 0.0) {
                    newBalance = 0.0;
                }
                lb.setBalanceUnits(newBalance);
            }

            lb.setLastAccrualDate(policy.getValidityEndDate());
            lb.setNextAccrualDate(policy.getValidityEndDate().plusDays(1));
        }

        leaveBalanceAdapter.saveLeaveBalances(leaveBalances);
    }

    @Override
    @Transactional
    public void assignPolicies(TimeOffPolicyBulkAssignModel request) {

        Set<String> finalUsers = getFinalUserSet(request.getUserIds(), request.getGroupIds());

        if (finalUsers.isEmpty() || request.getPolicyIds().isEmpty())
            return;

        List<TimeoffPolicyEntity> policies = timeoffPolicyAdapter.findPoliciesByIds(request.getPolicyIds());
        if (policies.isEmpty())
            throw new IllegalArgumentException("No valid policies found");

        List<UserPolicyEntity> existingAssignments =
                userPolicyAdapter.findUserPolicyEntities(new ArrayList<>(finalUsers));

        Map<String, Set<String>> existingMap = new HashMap<>();
        for (UserPolicyEntity u : existingAssignments) {
            existingMap
                    .computeIfAbsent(u.getUser().getUserId(), k -> new HashSet<>())
                    .add(u.getPolicy().getPolicyId());
        }

        List<UserPolicyEntity> assignList = new ArrayList<>();
        List<LeaveBalanceEntity> balanceList = new ArrayList<>();

        for (TimeoffPolicyEntity policy : policies) {

            LocalDate validFrom = policy.getValidityStartDate();
            LocalDate validTo = policy.getValidityEndDate();

            Double totalUnits = (policy.getEntitledUnits() != null)
                    ? policy.getEntitledUnits().doubleValue()
                    : policy.getEntitledHours().doubleValue();


            for (String userId : finalUsers) {

                Set<String> userPolicies = existingMap.getOrDefault(userId, new HashSet<>());

                if (userPolicies.contains(policy.getPolicyId()))
                    continue;

                UserEntity userEntity = userAdapter.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User ID " + userId + " not found."));

                assignList.add(buildUserPolicy(policy, userEntity, validFrom, validTo));

                balanceList.add(buildLeaveBalance(policy, userId, validFrom, validTo, totalUnits));

                userPolicies.add(policy.getPolicyId());
                existingMap.put(userId, userPolicies);
            }
        }

        if (!assignList.isEmpty())
            userPolicyAdapter.saveUserPolicies(assignList);

        if (!balanceList.isEmpty())
            leaveBalanceAdapter.saveLeaveBalances(balanceList);
    }

    @Override
    @Transactional
    public void inactivatePolicy(String policyId, TimeOffPolicyInactivateModel model) {

        TimeoffPolicyEntity policy = timeoffPolicyAdapter.findByPolicyId(policyId);

        if (policy == null) {
            throw new IllegalArgumentException("Invalid Policy ID");
        }

        Boolean status = model.getActive();
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        policy.setActive(status);
        policy.setUpdatedAt(LocalDateTime.now());

        timeoffPolicyAdapter.savePolicy(policy);
    }

    @Override
    public void createRequest(TimeOffRequest request) {
        TimeoffRequestEntity entity = new TimeoffRequestEntity();
        TimeoffPolicyEntity policy = timeoffPolicyAdapter.findPolicyById(request.getPolicyId());
        LeaveBalanceEntity leaveBalance = timeoffPolicyAdapter.findLeaveBalance(request.getPolicyId(),request.getUserId());
        boolean exists = timeoffPolicyAdapter.existsTimeoffRequest(request.getUserId(), request.getPolicyId());
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
        TimeoffRequestEntity saved = timeoffPolicyAdapter.saveRequest(entity);
        List<UsersRequestMappingEntity> usersMapping =
                Stream.concat(
                        Stream.of(buildMapping(ViewerType.APPROVER, request.getTo(), request.getUserId(), saved.getTimeoffRequestId())),
                        request.getCc().stream().map(viewer -> buildMapping(ViewerType.VIEWER, viewer, request.getUserId(), saved.getTimeoffRequestId()))
                ).toList();
        List<UsersRequestMappingEntity> entities = timeoffPolicyAdapter.saveUsersRequestMapping(usersMapping);
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
        TimeoffRequestEntity entity = timeoffPolicyAdapter.findByUserIdAndRequestDate(model.getUserId(), model.getRequestDate());
        LeaveBalanceEntity leaveBalance = timeoffPolicyAdapter.findLeaveBalance(entity.getPolicy().getPolicyId(), entity.getUserId());
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
        timeoffPolicyAdapter.saveRequest(entity);
    }

    private boolean handleEmployeeRules(Status current, Status next) {
        return (current == Status.PENDING || current == Status.APPROVED) && next == Status.CANCELLED;
    }

    @Override
    public void adminUpdateStatus(AdminStatusUpdate model) {
        TimeoffRequestEntity entity = timeoffPolicyAdapter.findByUserIdAndRequestDate(model.getUserId(), model.getRequestDate());
        if (LocalDate.now().isAfter(entity.getStartDate()) || handleEmployeeRules(entity.getStatus(), model.getStatus())) {
            throw new IllegalArgumentException("Update not allowed: leave started or invalid status.");
        }
        if (model.getStatus() != null) {
            entity.setStatus(model.getStatus());
        }
        timeoffPolicyAdapter.saveRequest(entity);
    }

    private boolean handleAdminRules(Status current, Status next) {
        return switch (current) {
            case PENDING -> next == Status.APPROVED || next == Status.REJECTED;
            case APPROVED -> next == Status.REJECTED;
            default -> false;
        };
    }

    public void updateLeaveBalance(){
        List<TimeoffRequestEntity> entities = timeoffPolicyAdapter.findStartByDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
        List<LeaveBalanceEntity> leaveBalanceEntities = new ArrayList<>();
        for(TimeoffRequestEntity entity : entities){
            LeaveBalanceEntity leaveBalance = timeoffPolicyAdapter.findLeaveBalance(entity.getPolicy().getPolicyId(), entity.getUserId());
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
        timeoffPolicyAdapter.saveAllLeaveBalance(leaveBalanceEntities);
    }

    @Override
    public List<TimeoffPoliciesModel> getAllPolicies() {
        List<TimeoffPolicyEntity> entities = timeoffPolicyAdapter.findAll();
        if (entities == null || entities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No Data Found");
        }
        return entities.stream().map(entity -> {
                    TimeoffPoliciesModel model = timeOffPolicyEntityMapper.toModel(entity);
                    List<String> userIds = timeoffPolicyAdapter.findUserIdsByPolicyId(entity.getPolicyId());
                    List<String> usernames = userIds.stream()
                            .map(timeoffPolicyAdapter::findUsernameByUserId)
                            .toList();
                    model.setAssignedUsernames(usernames);
                    return model;
                })
                .toList();
    }

    @Override
    public List<TimeoffPoliciesModel> getAllPolicy() {
        List<TimeoffPolicyEntity> entities= timeoffPolicyAdapter.findAll();
        if (entities==null||entities.isEmpty())throw new  ResponseStatusException(HttpStatus.CONFLICT, "No Data Found");
        return entities.stream().map(timeOffPolicyEntityMapper::toModel).toList();
    }

    @Override
    public List<AccrualTypeEnumModel> getAccrualTypeStatus() {
        List<AccrualTypeEnumModel> list = new ArrayList<>();
        for (AccrualType e : AccrualType.values()) {
            AccrualTypeEnumModel model = new AccrualTypeEnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public List<CompensationEnumModel> getCompensation() {
        List<CompensationEnumModel> list=new ArrayList<>();
        for(Compensation e:Compensation.values()){
            CompensationEnumModel model= new CompensationEnumModel(e.name(),e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public TimeoffPoliciesModel getPolicyById(String id) {
        TimeoffPolicyEntity entity=timeoffPolicyAdapter.findById(id);

        if (entity==null) new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No time-off policy found for userId");
        return timeOffPolicyEntityMapper.toModel(entity);
    }

    @Override
    public List<TimeoffPoliciesModel> getPolicyByUserId(String userId){
        List<TimeoffPolicyEntity> entity=timeoffPolicyAdapter.findByUserId(userId);
        if (entity==null) new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No time-off policy found for userId");
        return timeOffPolicyEntityMapper.toModelList(entity);
    }

    @Override
    public List<TimeoffRequestResponseModel> getRequestsByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");}
        List<TimeoffRequestUserModel> list = timeoffPolicyAdapter.filterWithUser(fromDate, toDate);
        List<TimeoffRequestResponseModel> response = new ArrayList<>();
        for (TimeoffRequestUserModel item : list) {
            TimeoffRequestResponseModel model = timeOffPolicyEntityMapper.toModel(item.getRequest());
            model.setUserName(item.getUserName());
            response.add(model);
        }

        return response;
    }



    private LeaveBalanceEntity buildLeaveBalance(TimeoffPolicyEntity policy,
                                                 String userId,
                                                 LocalDate validFrom,
                                                 LocalDate validTo,
                                                 double totalUnits) {

        LeaveBalanceEntity lb = new LeaveBalanceEntity();
        lb.setUserId(userId);
        lb.setPolicy(policy);
        lb.setPeriodStartDate(validFrom);
        lb.setPeriodEnd(validTo);
        lb.setTotalUnits(totalUnits);
        lb.setBalanceUnits(totalUnits);
        lb.setLeaveTakenUnits(0.0);
        lb.setCarryForwardUnits(0.0);
        lb.setLastAccrualDate(validTo);
        lb.setNextAccrualDate(validTo.plusDays(1));
        return lb;
    }

    private UserPolicyEntity buildUserPolicy(TimeoffPolicyEntity policy,
                                             UserEntity user,
                                             LocalDate validFrom,
                                             LocalDate validTo) {

        UserPolicyEntity up = new UserPolicyEntity();
        up.setUser(user);
        up.setPolicy(policy);
        up.setValidFrom(validFrom);
        up.setValidTo(validTo);
        up.setAssignedAt(LocalDateTime.now());
        return up;
    }

    private double calculateTotalUnits(TimeoffPolicyEntity policy, EntitledType type) {

        if (policy.getEntitledUnits() != null) {
            return (type == EntitledType.HALF_DAY)
                    ? policy.getEntitledUnits() * 0.5
                    : policy.getEntitledUnits().doubleValue();
        }

        return policy.getEntitledHours().doubleValue();
    }


    private Set<String> getFinalUserSet(List<String> userIds, List<Long> groupIds) {

        Set<String> finalUsers = new HashSet<>();

        if (userIds != null)
            finalUsers.addAll(userIds);

        if (groupIds != null && !groupIds.isEmpty()) {
            finalUsers.addAll(userAdapter.findUserIdsByGroupIds(groupIds));
        }

        return finalUsers;
    }

    @Override
    public List<TimeoffRequestResponseModel> filterRequestsByRole(
            LocalDate fromDate,
            LocalDate toDate,
            int minRoleLevel) {

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }

        List<TimeoffRequestUserModel> list =
                timeoffPolicyAdapter.filterWithUserAndRole(fromDate, toDate, minRoleLevel);

        List<TimeoffRequestResponseModel> result = new ArrayList<>();

        for (TimeoffRequestUserModel item : list) {
            TimeoffRequestResponseModel rm =
                    timeOffPolicyEntityMapper.toModel(item.getRequest());

            rm.setUserName(item.getUserName());
            result.add(rm);
        }

        return result;
    }



}
