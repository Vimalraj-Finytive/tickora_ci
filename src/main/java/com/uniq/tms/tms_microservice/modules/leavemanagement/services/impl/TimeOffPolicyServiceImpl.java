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
                throw new IllegalArgumentException("userValidFrom and userValidTo are required for FIXED accrual.");
            }
            if (accrual == AccrualType.FIXED) {

                if (request.getEntitledType() != EntitledType.DAY) {
                    throw new IllegalArgumentException(
                            "For FIXED accrual, entitledType must be DAY. HALF_DAY or HOURS are not allowed."
                    );
                }
            }
        }
        else {
            if (reset == null) {
                throw new IllegalArgumentException("resetFrequency is required.");
            }

            if (accrual == AccrualType.ANNUALLY && reset != ResetFrequency.ANNUALLY) {
                throw new IllegalArgumentException("resetFrequency must be ANNUALLY for ANNUALLY accrual type.");
            }

        }

        if (request.getUserValidFrom().isBefore(request.getValidityStartDate())) {
            throw new IllegalArgumentException(
                    "User Valid From must be on or after validity start date."
            );
        }

        if (request.getValidityEndDate() != null && request.getUserValidTo() != null && request.getUserValidTo().isAfter(request.getValidityEndDate())) {
            throw new IllegalArgumentException(
                    "User Valid To must be on or before validity end date."
            );
        }

        if (request.getUserValidTo() != null && request.getUserValidTo().isBefore(request.getUserValidFrom())) {
            throw new IllegalArgumentException(
                    "User Valid To cannot be earlier than user valid from."
            );
        }


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

        policy.setAccrualStartDate(LocalDate.now());
        if (policy.getAccrualType() == AccrualType.FIXED) {
            policy.setResetFrequency(null);
        } else {
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

        TimeOffPolicyEntity policy = timeoffPolicyAdapter.findByPolicyId(request.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("Invalid Policy ID");
        }

        if (request.getUserValidFrom().isBefore(request.getValidityStartDate())) {
            throw new IllegalArgumentException(
                    "User Valid From must be on or after validity start date."
            );
        }

        if (request.getValidityEndDate() != null && request.getUserValidTo() != null && request.getUserValidTo().isAfter(request.getValidityEndDate())) {
            throw new IllegalArgumentException(
                    "User Valid To must be on or before validity end date."
            );
        }

        if (request.getUserValidTo() != null && request.getUserValidTo().isBefore(request.getUserValidFrom())) {
            throw new IllegalArgumentException(
                    "User Valid To cannot be earlier than user valid from."
            );
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
                lb.setPeriodStartDate(policy.getValidityStartDate());

            if (request.getValidityEndDate() != null)
                lb.setPeriodEnd(policy.getValidityEndDate());

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

        List<TimeOffPolicyEntity> policies = timeoffPolicyAdapter.findPoliciesByIds(request.getPolicyIds());
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

        for (TimeOffPolicyEntity policy : policies) {

            LocalDate userFrom;
            LocalDate userTo;

            if (policy.getAccrualType() == AccrualType.FIXED) {

                if (request.getUserValidFrom() == null) {
                    throw new IllegalArgumentException(
                            "User Valid From is required for FIXED accrual policies."
                    );
                }

                userFrom = request.getUserValidFrom();

                if (request.getUserValidTo() != null) {
                    userTo = request.getUserValidTo();
                }
                else {
                    ResetFrequency reset = policy.getResetFrequency();

                    if (reset == ResetFrequency.MONTHLY) {
                        userTo = userFrom.plusMonths(1);
                    } else if (reset == ResetFrequency.ANNUALLY) {
                        userTo = userFrom.plusYears(1);
                    } else {
                        throw new IllegalArgumentException(
                                "Fixed accrual requires resetFrequency MONTHLY or ANNUALLY to auto-calculate end date."
                        );
                    }
                }
            }
            else {
                userFrom = request.getUserValidFrom();
                userTo = request.getUserValidTo();
            }
            Double totalUnits = (policy.getEntitledUnits() != null)
                    ? policy.getEntitledUnits().doubleValue()
                    : policy.getEntitledHours().doubleValue();


            for (String userId : finalUsers) {

                Set<String> userPolicies = existingMap.getOrDefault(userId, new HashSet<>());

                if (userPolicies.contains(policy.getPolicyId()))
                    continue;

                UserEntity userEntity = userAdapter.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User ID " + userId + " not found."));

                assignList.add(buildUserPolicy(policy, userEntity, userFrom, userTo));

                LocalDate nextSchedulerRun = LocalDate.now().plusMonths(1).withDayOfMonth(1);

                if (userFrom.isBefore(nextSchedulerRun)) {
                    balanceList.add(buildLeaveBalance(policy, userId, userFrom, userTo, totalUnits));
                }

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

    private LeaveBalanceEntity buildLeaveBalance(TimeOffPolicyEntity policy,
                                                 String userId,
                                                 LocalDate validFrom,
                                                 LocalDate validTo,
                                                 double totalUnits) {
        LeaveBalanceEntity lb = new LeaveBalanceEntity();
        lb.setUserId(userId);
        lb.setPolicy(policy);
        LocalDate computedValidTo = validTo;

        if (computedValidTo == null) {
            if (policy.getAccrualType() == AccrualType.MONTHLY) {
                computedValidTo = validFrom.withDayOfMonth(validFrom.lengthOfMonth());
            }
            else if (policy.getAccrualType() == AccrualType.ANNUALLY) {
                computedValidTo = LocalDate.of(validFrom.getYear(), 12, 31);
            }
        }
        lb.setPeriodStartDate(validFrom);
        lb.setPeriodEnd(computedValidTo);
        lb.setTotalUnits(totalUnits);
        lb.setBalanceUnits(totalUnits);
        lb.setLeaveTakenUnits(0.0);
        lb.setCarryForwardUnits(0.0);
        lb.setLastAccrualDate(validTo);
        lb.setNextAccrualDate(validTo.plusDays(1));
        return lb;
    }

    private UserPolicyEntity buildUserPolicy(TimeOffPolicyEntity policy,
                                             UserEntity user,
                                             LocalDate validFrom,
                                             LocalDate validTo) {

        UserPolicyEntity up = new UserPolicyEntity();
        up.setUser(user);
        up.setPolicy(policy);
        up.setValidFrom(validFrom);
        up.setValidTo(validTo);
        up.setAssignedAt(LocalDateTime.now());
        if (policy.getAccrualType() == AccrualType.FIXED) {
            up.setEntitledUnits(policy.getEntitledUnits());
        } else {
            up.setEntitledUnits(null);
        }
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

}
