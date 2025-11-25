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
import java.util.*;

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

        ResetFrequency reset = request.getResetFrequency();
        AccrualType accrual = request.getAccrualType();

        if (accrual == AccrualType.FIXED) {
            reset = null;
            if (request.getUserValidFrom() == null || request.getUserValidTo() == null) {
                throw new IllegalArgumentException("userValidFrom and userValidTo are required for FIXED accrual");
            }
                if (request.getEntitledType() != EntitledType.DAY) {throw new IllegalArgumentException(
                            "For FIXED accrual, entitledType must be DAY");
                }
        }
        else {
            if (reset == null) {
                throw new IllegalArgumentException("resetFrequency is required.");
            }

            if (accrual == AccrualType.ANNUALLY && reset != ResetFrequency.ANNUALLY) {throw new IllegalArgumentException(
                    "For ANNUALLY accrual type, MONTHLY resetFrequency is not allowed.");
            }

        }
        validateUserValidDates(request.getUserValidFrom(), request.getUserValidTo(), request.getValidityStartDate(), request.getValidityEndDate());

        String policyId = idGenerationService.generateNextTimeOffPolicyId();

        TimeOffPolicyEntity policy = timeOffPolicyEntityMapper.toEntity(request);
        policy.setPolicyId(policyId);

        if (request.getEntitledType() == EntitledType.DAY || request.getEntitledType() == EntitledType.HALF_DAY) {

            policy.setEntitledUnits(request.getEntitledUnits());
            policy.setEntitledHours(null);

        } else {
            policy.setEntitledUnits(null);
            policy.setEntitledHours(request.getEntitledHours());
        }

        if (policy.getAccrualType() == AccrualType.FIXED) {
            policy.setAccrualStartDate(LocalDate.now());
            policy.setResetFrequency(null);
        } else {
            policy.setAccrualStartDate(LocalDate.now());
            policy.setResetFrequency(request.getResetFrequency());
        }

        policy.setValidityStartDate(request.getValidityStartDate());
        policy.setValidityEndDate(request.getValidityEndDate());
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

        LocalDate validFrom = request.getUserValidFrom();

        LocalDate validTo = request.getUserValidTo();

        double totalUnits = calculateTotalUnits(policy, request.getEntitledType());

        for (String userId : finalUsers) {

            UserEntity userEntity = userAdapter.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User ID " + userId + " not found."));
            userPolicies.add(buildUserPolicy(policy, userEntity, validFrom, validTo));
            if (shouldCreateLeaveBalance(validFrom)) {
                leaveBalances.add(buildLeaveBalance(policy, userId, validFrom, validTo, totalUnits));
            }
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

        TimeOffPolicyEntity policy = timeoffPolicyAdapter.findByPolicyId(request.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("Invalid Policy ID");
        }

        validateUserValidDates(request.getUserValidFrom(), request.getUserValidTo(), request.getValidityStartDate(), request.getValidityEndDate());

        if (request.getEntitledUnits() != null && policy.getEntitledUnits() != null) {
            if (request.getEntitledUnits() < policy.getEntitledUnits()) {throw new IllegalArgumentException(
                        "You cannot reduce entitled units");
            }
        }

        if (request.getEntitledHours() != null && policy.getEntitledHours() != null) {
            if (request.getEntitledHours() < policy.getEntitledHours()) {throw new IllegalArgumentException(
                        "You cannot reduce entitled hours");
            }
        }

        if (policy.getEntitledType() == EntitledType.DAY || policy.getEntitledType() == EntitledType.HALF_DAY) {
            if (policy.getEntitledHours() != null) {throw new IllegalArgumentException(
                    "Entitled hours are not allowed when entitled type is DAY or HALF_DAY.");
            }
        }

        if (policy.getEntitledType() == EntitledType.HOURS) {
            if (policy.getEntitledUnits() != null) {throw new IllegalArgumentException(
                        "Entitled units are not allowed when entitled type is HOURS.");
            }
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
                if (request.getMaxCarryForwardUnits() == null) {throw new IllegalArgumentException(
                            "MaxCarryForwardUnits is required when carryForward is TRUE");
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

        List<UserPolicyEntity> assignedUsers =
                userPolicyAdapter.findUserPoliciesByPolicyId(policy.getPolicyId());

        if (assignedUsers.isEmpty())
            return;

        for (UserPolicyEntity up : assignedUsers) {
            up.setValidFrom(request.getUserValidFrom());
            up.setValidTo(request.getUserValidTo());
        }

        userPolicyAdapter.saveUserPolicies(assignedUsers);

        if (!entitlementChanged)
            return;

        double totalUnits = calculateTotalUnits(policy, policy.getEntitledType());

        List<LeaveBalanceEntity> leaveBalances =
                leaveBalanceAdapter.findLeaveBalancesByPolicyId(policy.getPolicyId());

        for (LeaveBalanceEntity lb : leaveBalances) {
            if (request.getValidityStartDate() != null)
                lb.setPeriodStartDate(request.getUserValidFrom());

            if (request.getValidityEndDate() != null)
                lb.setPeriodEnd(request.getUserValidTo());

            lb.setTotalUnits(totalUnits);

            double taken = lb.getLeaveTakenUnits();

            if (taken == 0.0) {
                lb.setBalanceUnits(totalUnits);
            } else {
                Double newBalance = totalUnits - taken;

                if (newBalance < 0.0) {
                    newBalance = 0.0;
                }
                lb.setBalanceUnits(newBalance);
            }

            lb.setLastAccrualDate(request.getUserValidFrom());
            LocalDate nextAccrual = resolveNextAccrualDate(request.getUserValidFrom(), request.getUserValidTo(), policy.getValidityEndDate(), policy.getAccrualType());
            lb.setNextAccrualDate(nextAccrual);
        }
        leaveBalanceAdapter.saveLeaveBalances(leaveBalances);
    }

    @Override
    @Transactional
    public void assignPolicies(TimeOffPolicyBulkAssignModel request) {

        Set<String> finalUsers = getFinalUserSet(request.getUserIds(), request.getGroupIds());

        if (finalUsers.isEmpty() || request.getPolicyIds().isEmpty()) {
            return;
        }

        List<TimeOffPolicyEntity> policies = timeoffPolicyAdapter.findPoliciesByIds(request.getPolicyIds());
        if (policies.isEmpty()) {
            throw new IllegalArgumentException("No valid policies found");
        }

        List<UserPolicyEntity> assignList = new ArrayList<>();
        List<LeaveBalanceEntity> balanceList = new ArrayList<>();

        for (TimeOffPolicyEntity policy : policies) {

            LocalDate userFrom = request.getUserValidFrom();
            LocalDate userTo = request.getUserValidTo();

            if (userFrom == null) {
                throw new IllegalArgumentException("User Valid From is required");
            }

            Double totalUnits = (policy.getEntitledUnits() != null)
                    ? policy.getEntitledUnits().doubleValue()
                    : policy.getEntitledHours().doubleValue();

            for (String userId : finalUsers) {

                UserEntity userEntity = userAdapter.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("User ID " + userId + " not found."));

                UserPolicyEntity upe = buildUserPolicy(policy, userEntity, userFrom, userTo);
                assignList.add(upe);

                if (shouldCreateLeaveBalance(userFrom)) {
                    LeaveBalanceEntity lb = buildLeaveBalance(policy, userId, userFrom, userTo, totalUnits);
                    balanceList.add(lb);
                }

            }
        }

        if (!assignList.isEmpty()) {
            userPolicyAdapter.saveUserPolicies(assignList);
        }

        if (!balanceList.isEmpty()) {
            leaveBalanceAdapter.saveLeaveBalances(balanceList);
        }
    }



    @Override
    @Transactional
    public void inactivatePolicy(String policyId, TimeOffPolicyInactivateModel model) {

        TimeOffPolicyEntity policy = timeoffPolicyAdapter.findByPolicyId(policyId);

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
    public List<TimeOffPoliciesModel> getAllPolicies() {
        List<TimeOffPolicyEntity> entities = timeoffPolicyAdapter.findAll();
        if (entities == null || entities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No Data Found");
        }
        return entities.stream().map(entity -> {
                    TimeOffPoliciesModel model = timeOffPolicyEntityMapper.toModel(entity);
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
    public List<TimeOffPoliciesModel> getAllPolicy() {
        List<TimeOffPolicyEntity> entities= timeoffPolicyAdapter.findAll();
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
    public TimeOffPoliciesModel getPolicyById(String id) {
        TimeOffPolicyEntity entity=timeoffPolicyAdapter.findById(id);

        if (entity==null) new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No time-off policy found for userId");
        return timeOffPolicyEntityMapper.toModel(entity);
    }

    @Override
    public List<TimeOffPoliciesModel> getPolicyByUserId(String userId){
        List<TimeOffPolicyEntity> entity=timeoffPolicyAdapter.findByUserId(userId);
        if (entity==null) new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No time-off policy found for userId");
        return timeOffPolicyEntityMapper.toModelList(entity);
    }

    private LeaveBalanceEntity buildLeaveBalance(TimeOffPolicyEntity policy, String userId, LocalDate validFrom, LocalDate validTo, double totalUnits) {

        LeaveBalanceEntity lb = new LeaveBalanceEntity();

        UserEntity userEntity = userAdapter.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User ID " + userId + " not found."));

        lb.setUser(userEntity);
        lb.setPolicy(policy);

        LocalDate computedValidTo = validTo;

        if (computedValidTo == null) {
            if (policy.getAccrualType() == AccrualType.MONTHLY) {
                computedValidTo = validFrom.withDayOfMonth(validFrom.lengthOfMonth());
            } else if (policy.getAccrualType() == AccrualType.ANNUALLY) {
                computedValidTo = LocalDate.of(validFrom.getYear(), 12, 31);
            }
        }

        lb.setPeriodStartDate(validFrom);
        lb.setPeriodEnd(computedValidTo);

        lb.setTotalUnits(totalUnits);
        lb.setBalanceUnits(totalUnits);
        lb.setLeaveTakenUnits(0.0);
        lb.setCarryForwardUnits(0.0);

        lb.setLastAccrualDate(computedValidTo);

        LocalDate nextAccrualDate = resolveNextAccrualDate(validFrom, computedValidTo, policy.getValidityEndDate(), policy.getAccrualType());
        lb.setNextAccrualDate(nextAccrualDate);
        return lb;
    }


    private UserPolicyEntity buildUserPolicy(TimeOffPolicyEntity policy, UserEntity user, LocalDate validFrom, LocalDate validTo) {
        UserPolicyEntity up = new UserPolicyEntity();
        up.setUser(user);
        up.setPolicy(policy);
        up.setValidFrom(validFrom);
        up.setValidTo(validTo);

        if (policy.getAccrualType() == AccrualType.FIXED) {
            up.setEntitledUnits(policy.getEntitledUnits());
        } else {
            up.setEntitledUnits(null);
        }
        up.setAssignedAt(LocalDateTime.now());
        return up;
    }


    private double calculateTotalUnits(TimeOffPolicyEntity policy, EntitledType type) {

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

    private boolean shouldCreateLeaveBalance(LocalDate userValidFrom) {
        LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate nextMonthStart = currentMonthStart.plusMonths(1);
        return ( !userValidFrom.isBefore(currentMonthStart) && userValidFrom.isBefore(nextMonthStart) );
    }

    private void validateUserValidDates(LocalDate userValidFrom, LocalDate userValidTo, LocalDate validityStartDate, LocalDate validityEndDate) {
        if (userValidFrom != null && validityStartDate != null &&
                userValidFrom.isBefore(validityStartDate)) {
            throw new IllegalArgumentException(
                    "User valid from date cannot be earlier than the policy start date."
            );
        }

        if (validityEndDate != null && userValidTo != null &&
                userValidTo.isAfter(validityEndDate)) {
            throw new IllegalArgumentException(
                    "User valid to date cannot be later than the policy end date."
            );
        }

        if (userValidTo != null && userValidFrom != null &&
                userValidTo.isBefore(userValidFrom)) {
            throw new IllegalArgumentException(
                    "User valid to date cannot be earlier than the user valid from date."
            );
        }
    }

    private LocalDate calculateNextAccrualDate(LocalDate start, AccrualType accrualType) {

        if (accrualType == AccrualType.MONTHLY) {
            return start.plusMonths(1).withDayOfMonth(1);
        }
        else if (accrualType == AccrualType.ANNUALLY) {
            return start.plusYears(1).withMonth(1).withDayOfMonth(1);
        }
        return null;
    }


    private LocalDate resolveNextAccrualDate(LocalDate validFrom, LocalDate validTo, LocalDate policyValidityEnd, AccrualType accrualType) {
        LocalDate today = LocalDate.now();
        LocalDate nextAccrualDate = null;

        if (validTo != null && policyValidityEnd != null) {
            boolean endsSame = validTo.getYear() == policyValidityEnd.getYear() && validTo.getMonth() == policyValidityEnd.getMonth();
            if (!endsSame) {
                nextAccrualDate = calculateNextAccrualDate(validFrom, accrualType);
            }
        }

        else if (validTo != null) {
            boolean endsThisMonth = validTo.getYear() == today.getYear() && validTo.getMonth() == today.getMonth();
            if (!endsThisMonth) {
                nextAccrualDate = calculateNextAccrualDate(validFrom, accrualType);
            }
        }

        else if (policyValidityEnd != null) {
            boolean endsThisMonth = policyValidityEnd.getYear() == today.getYear() && policyValidityEnd.getMonth() == today.getMonth();
            if (!endsThisMonth) {
                nextAccrualDate = calculateNextAccrualDate(validFrom, accrualType);
            }
        }

        else {
            nextAccrualDate = calculateNextAccrualDate(validFrom, accrualType);
        }
        return nextAccrualDate;
    }

}
