package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
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
import java.util.stream.Collectors;

@Service
public class TimeOffPolicyServiceImpl implements TimeOffPolicyService {

    private static final Logger log = LoggerFactory.getLogger(TimeOffPolicyServiceImpl.class);

    private final IdGenerationService idGenerationService;
    private final UserAdapter userAdapter;
    private final TimeOffPolicyEntityMapper timeOffPolicyEntityMapper;
    private final UserPolicyAdapter userPolicyAdapter;
    private final LeaveBalanceAdapter leaveBalanceAdapter;
    private final TimeOffPolicyAdapter timeOffPolicyAdapter;

    public TimeOffPolicyServiceImpl(IdGenerationService idGenerationService, UserAdapter userAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper, UserPolicyAdapter userPolicyAdapter, LeaveBalanceAdapter leaveBalanceAdapter,
                                    TimeOffPolicyAdapter timeOffPolicyAdapter) {
        this.idGenerationService = idGenerationService;
        this.userAdapter = userAdapter;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
        this.userPolicyAdapter = userPolicyAdapter;
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
    }

    @Override
    @Transactional
    public TimeOffPolicyResponseModel createPolicy(TimeOffPolicyRequestModel request) {

        if (request.getCompensation() == Compensation.UNPAID) {
            if (request.getEntitledType() != null ||
                    request.getEntitledUnits() != null ||
                    Boolean.TRUE.equals(request.getCarryForward()) ||
                    request.getMaxCarryForwardUnits() != null) {
                throw new IllegalArgumentException(
                        "Entitlement and carry-forward fields are not allowed for UNPAID compensation."
                );
            }
        }

        if (request.getEntitledType() != EntitledType.DAY) {

            if (Boolean.TRUE.equals(request.getCarryForward()) || request.getMaxCarryForwardUnits() != null) {
                throw new IllegalArgumentException("Carry forward is allowed only for DAY entitled type.");
            }

            request.setCarryForward(false);
            request.setMaxCarryForwardUnits(0);
        }


        ResetFrequency reset = request.getResetFrequency();
        AccrualType accrual = request.getAccrualType();

        if (accrual == AccrualType.FIXED) {
            if (reset != null){
                throw new IllegalArgumentException("For FIXED accrual reset frequency must be null");
            }
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

            if (accrual == AccrualType.ANNUALLY && reset != ResetFrequency.ANNUALLY) {
                throw new IllegalArgumentException("For ANNUALLY accrual type, MONTHLY resetFrequency is not allowed.");
            }

        }

        validateUserValidDates(request.getUserValidFrom(), request.getUserValidTo(), request.getValidityStartDate(), request.getValidityEndDate());

        String policyId = idGenerationService.generateNextTimeOffPolicyId();

        TimeOffPolicyEntity policy = timeOffPolicyEntityMapper.toEntity(request);
        policy.setPolicyId(policyId);

        if (request.getEntitledUnits() !=null) {
            policy.setEntitledUnits(request.getEntitledUnits());
        }

        if (policy.getAccrualType() == AccrualType.FIXED) {
            policy.setAccrualStartDate(LocalDate.now());
            policy.setResetFrequency(null);
        } else {
            policy.setAccrualStartDate(LocalDate.now());
            policy.setResetFrequency(request.getResetFrequency());
        }

        if (request.getCarryForward().equals(Boolean.TRUE)) {
            policy.setCarryForward(request.getCarryForward());
            if (request.getMaxCarryForwardUnits() == null) {
                throw new IllegalArgumentException("MaxCarryForwardUnits is required");
            }
            policy.setMaxCarryForwardUnits(request.getMaxCarryForwardUnits());

        }
        policy.setValidityStartDate(request.getValidityStartDate());
        policy.setValidityEndDate(request.getValidityEndDate());
        policy.setActive(true);
        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());
        policy = timeOffPolicyAdapter.savePolicy(policy);

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

        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findByPolicyId(request.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("Invalid Policy ID");
        }

        LocalDate today = LocalDate.now();
        LocalDate userFrom = request.getUserValidFrom();
        LocalDate userTo = request.getUserValidTo();
        LocalDate endDate = request.getValidityEndDate();

        if (userFrom != null && userFrom.isBefore(today)) {
            throw new IllegalArgumentException("User valid-from date should not be earlier than today");
        }

        if (policy.getValidityEndDate() != null && userFrom != null &&
                userFrom.isAfter(policy.getValidityEndDate())) {
            throw new IllegalArgumentException("User valid-from should not be after policy validity end date");
        }

        if (policy.getAccrualType() == AccrualType.FIXED && userTo == null) {
            throw new IllegalArgumentException("For FIXED policies, userValidTo is required");
        }

        if (userTo != null && policy.getValidityEndDate() != null &&
                userTo.isAfter(policy.getValidityEndDate())) {
            throw new IllegalArgumentException("User end date should not exceed policy validity end date");
        }

        if (userTo != null && endDate != null && userTo.isAfter(endDate)) {
            throw new IllegalArgumentException("User end date should not exceed period end date");
        }


        if (policy.getEntitledType() != EntitledType.DAY && (Boolean.TRUE.equals(request.getCarryForward()) || request.getMaxCarryForwardUnits() != null)) {
            throw new IllegalArgumentException("Carry forward is allowed only for DAY");
        }

        boolean entitlementChanged = false;

        if (request.getPolicyName() != null && !request.getPolicyName().trim().isEmpty()) {
            policy.setPolicyName(request.getPolicyName().trim());
        }

        if (request.getEntitledUnits() !=null) {
            policy.setEntitledUnits(request.getEntitledUnits());
            entitlementChanged = true;
        }

        if (request.getCarryForward().equals(Boolean.TRUE)) {
            policy.setCarryForward(request.getCarryForward());
                if (request.getMaxCarryForwardUnits() == null) {
                    throw new IllegalArgumentException("MaxCarryForwardUnits is required");
                }
                policy.setMaxCarryForwardUnits(request.getMaxCarryForwardUnits());

        }

        policy.setUpdatedAt(LocalDateTime.now());
        timeOffPolicyAdapter.savePolicy(policy);

        List<UserPolicyEntity> assignedUsers = userPolicyAdapter.findUserPoliciesByPolicyId(policy.getPolicyId());

        List<LeaveBalanceEntity> assignedLb =leaveBalanceAdapter.findLeaveBalancesByPolicyId(request.getPolicyId());
        Set<String> existingUserIds = assignedUsers.stream()
                .map(up -> up.getUser().getUserId())
                .collect(Collectors.toSet());


        Set<String> newUserSet = getFinalUserSet(request.getUserIds(), request.getGroupIds());

        Set<String> removedUsers = existingUserIds.stream()
                .filter(u -> !newUserSet.contains(u))
                .collect(Collectors.toSet());

        Set<String> newUsersToAdd = newUserSet.stream()
                .filter(u -> !existingUserIds.contains(u))
                .collect(Collectors.toSet());

        Set<String> usersToUpdate = newUserSet.stream()
                .filter(existingUserIds::contains)
                .collect(Collectors.toSet());

        if (!removedUsers.isEmpty()) {
            userPolicyAdapter.deleteByPolicyIdAndUserIds(policy.getPolicyId(), removedUsers);
            leaveBalanceAdapter.deleteByPolicyIdAndUserIds(policy.getPolicyId(), removedUsers);
        }

        double totalUnits = request.getEntitledUnits() != null
                ? request.getEntitledUnits()
                : policy.getEntitledUnits();

        List<UserPolicyEntity> addList = new ArrayList<>();
        List<LeaveBalanceEntity> lbList = new ArrayList<>();


        for (String userId : newUsersToAdd) {

            UserEntity userEntity = userAdapter.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

            UserPolicyEntity upe = buildUserPolicy(policy, userEntity, userFrom, userTo);
            addList.add(upe);

            LeaveBalanceEntity lb = buildLeaveBalance(policy, userId, userFrom, userTo, totalUnits);
            lbList.add(lb);
        }

        if (!addList.isEmpty())
            userPolicyAdapter.saveUserPolicies(addList);

        if (!lbList.isEmpty())
            leaveBalanceAdapter.saveLeaveBalances(lbList);

        for (UserPolicyEntity up : assignedUsers) {
            if (usersToUpdate.contains(up.getUser().getUserId())) {
                if (request.getUserValidTo() != null) {
                    up.setValidTo(request.getUserValidTo());
                }
            }
        }
        for (LeaveBalanceEntity alb: assignedLb){
            if (usersToUpdate.contains(alb.getUser().getUserId())) {
                if (request.getUserValidTo() != null) {
                    double carryForwardUnits=request.getMaxCarryForwardUnits();
                    alb.setCarryForwardUnits(carryForwardUnits);
                    alb.setUpdatedAt(LocalDateTime.now());
                }
            }
        }

        userPolicyAdapter.saveUserPolicies(assignedUsers);

        if (!entitlementChanged)
            return;

        List<LeaveBalanceEntity> leaveBalances =
                leaveBalanceAdapter.findLeaveBalancesByPolicyId(policy.getPolicyId());

        for (LeaveBalanceEntity lb : leaveBalances) {

            if (request.getUserValidTo() != null)
                lb.setPeriodEnd(request.getUserValidTo());

            if (request.getEntitledUnits() != null) {
                double total = request.getEntitledUnits();
                lb.setTotalUnits(total);

                double taken = lb.getLeaveTakenUnits();
                double newBalance = total - taken;

                if (newBalance < 0) newBalance = 0;
                lb.setBalanceUnits(newBalance);
            }
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

        List<TimeOffPolicyEntity> policies = timeOffPolicyAdapter.findPoliciesByIds(request.getPolicyIds());
        if (policies.isEmpty()) {
            throw new IllegalArgumentException("No valid policies found");
        }

        LocalDate userFrom = request.getUserValidFrom();
        LocalDate userTo   = request.getUserValidTo();

        if (userFrom == null) {
            throw new IllegalArgumentException("User Valid From is required");
        }

        List<UserEntity> allUsers = userAdapter.findByUserId(new ArrayList<>(finalUsers));

        Map<String, UserEntity> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserEntity::getUserId, u -> u));

        List<UserPolicyEntity> existingAssignments =
                userPolicyAdapter.findAllByPolicyIdsAndUserIds(
                        request.getPolicyIds(),
                        finalUsers
                );

        Map<String, UserPolicyEntity> existingMap =
                existingAssignments.stream().collect(Collectors.toMap(
                        up -> up.getPolicy().getPolicyId() + "_" + up.getUser().getUserId(),
                        up -> up
                ));

        List<UserPolicyEntity> assignList = new ArrayList<>();
        List<LeaveBalanceEntity> balanceList = new ArrayList<>();
        for (TimeOffPolicyEntity policy : policies) {

            if (policy.getAccrualType() == AccrualType.FIXED && userTo == null) {
                throw new IllegalArgumentException("For FIXED policies userTo is required");
            }

            double totalUnits = policy.getEntitledUnits();

            for (String userId : finalUsers) {

                UserEntity userEntity = userMap.get(userId);

                if (userEntity == null) {
                    throw new IllegalArgumentException("User not found: " + userId);
                }

                String key = policy.getPolicyId() + "_" + userId;
                UserPolicyEntity existing = existingMap.get(key);

                if (existing != null) {

                    if (policy.getAccrualType() == AccrualType.FIXED) {

                        boolean expired = existing.getValidTo() != null &&
                                existing.getValidTo().isBefore(userFrom);

                        if (!expired) {
                            continue;
                        }
                    }
                    else {
                        continue;
                    }
                }

                UserPolicyEntity upe = buildUserPolicy(policy, userEntity, userFrom, userTo);
                assignList.add(upe);

                LeaveBalanceEntity lb = buildLeaveBalance(policy, userId, userFrom, userTo, totalUnits);

                balanceList.add(lb);
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

        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findByPolicyId(policyId);

        if (policy == null) {
            throw new IllegalArgumentException("Invalid Policy ID");
        }

        Boolean status = model.getActive();
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        policy.setActive(status);
        policy.setUpdatedAt(LocalDateTime.now());

        timeOffPolicyAdapter.savePolicy(policy);
    }

    @Override
    public List<TimeOffPoliciesModel> getAllPolicies() {
        List<TimeOffPolicyEntity> entities = timeOffPolicyAdapter.findByIsActiveTrue();
        if (entities == null || entities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No Data Found");
        }
        return entities.stream().map(entity -> {
                    TimeOffPoliciesModel model = timeOffPolicyEntityMapper.toModel(entity);
                    List<String> userIds = timeOffPolicyAdapter.findUserIdsByPolicyId(entity.getPolicyId());
                    List<String> usernames = userIds.stream()
                            .map(timeOffPolicyAdapter::findUsernameByUserId)
                            .toList();
                    model.setAssignedUsernames(usernames);
                    return model;
                })
                .toList();
    }

    @Override
    public List<TimeOffPoliciesModel> getAllPolicy() {
        List<TimeOffPolicyEntity> entities= timeOffPolicyAdapter.findByIsActiveTrue();
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
        TimeOffPolicyEntity entity=timeOffPolicyAdapter.findById(id);

        if (entity==null) new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No time-off policy found");
        return timeOffPolicyEntityMapper.toModel(entity);
    }

    @Override
    public List<TimeOffPoliciesModel> getPolicyByUserId(String userId){
        List<TimeOffPolicyEntity> entity=timeOffPolicyAdapter.findByUserId(userId);
        if (entity==null) new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No time-off policy found ");
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

        double carryForwardUnits = policy.getMaxCarryForwardUnits() == null ? 0 : policy.getMaxCarryForwardUnits();
        lb.setCarryForwardUnits(carryForwardUnits);

        lb.setLastAccrualDate(computedValidTo);
        LocalDate nextAccrualDate = resolveNextAccrualDate(validFrom, computedValidTo, policy.getValidityEndDate(), policy.getAccrualType());
        lb.setNextAccrualDate(nextAccrualDate);
        lb.setUpdatedAt(LocalDateTime.now());
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

        return policy.getEntitledUnits().doubleValue();
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
