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
import com.uniq.tms.tms_microservice.shared.dto.EnumModel;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

        boolean exists = timeOffPolicyAdapter.existsByPolicyNameIgnoreCase(request.getPolicyName());
        if (exists) {
            throw new IllegalArgumentException("Policy name already exists");
        }

        if (request.getCompensation() == Compensation.UNPAID) {
            if (request.getEntitledType() != null ||
                    request.getEntitledUnits() != null ||
                    Boolean.TRUE.equals(request.getCarryForward()) ||
                    request.getMaxCarryForwardUnits() != null ||
                    request.getAccrualType() != null) {

                throw new IllegalArgumentException(
                        "Accrual, Entitlement & Carry-Forward fields must be NULL for UNPAID policy."
                );
            }
        }

        if (request.getAccrualType() == AccrualType.FIXED &&
                Boolean.TRUE.equals(request.getCarryForward())) {
            throw new IllegalArgumentException("Carry-forward not allowed for FIXED policies.");
        }

        if (request.getMaxCarryForwardUnits() != null &&
                request.getEntitledUnits() != null &&
                request.getMaxCarryForwardUnits() > request.getEntitledUnits()) {

            throw new IllegalArgumentException("Carry forward units cannot exceed entitled units.");
        }

        if (Boolean.TRUE.equals(request.getCarryForward()) &&
                request.getMaxCarryForwardUnits() == null) {

            throw new IllegalArgumentException("maxCarryForwardUnits required when carryForward = true.");
        }

        if (request.getEntitledType() != EntitledType.DAY) {

            if (Boolean.TRUE.equals(request.getCarryForward()) ||
                    request.getMaxCarryForwardUnits() != null) {
                throw new IllegalArgumentException("Carry forward allowed only for DAY entitlement.");
            }

            request.setCarryForward(false);
            request.setMaxCarryForwardUnits(0);
        }

        ResetFrequency reset = request.getResetFrequency();
        AccrualType accrual = request.getAccrualType();

        if (accrual == AccrualType.FIXED) {
            if (reset != null) {
                throw new IllegalArgumentException("FIXED policies cannot have resetFrequency.");
            }
            if (request.getEntitledType() != EntitledType.DAY) {
                throw new IllegalArgumentException("FIXED policies require DAY entitlement.");
            }
        }

        if ((accrual == AccrualType.MONTHLY || accrual == AccrualType.ANNUALLY) && reset == null) {
            throw new IllegalArgumentException("resetFrequency required for MONTHLY/ANNUALLY policies.");
        }

        if (accrual == AccrualType.ANNUALLY && reset != ResetFrequency.ANNUALLY) {
            throw new IllegalArgumentException("Annual accrual must use ANNUALLY resetFrequency.");
        }

        String policyId = idGenerationService.generateNextTimeOffPolicyId();

        TimeOffPolicyEntity policy = timeOffPolicyEntityMapper.toEntity(request);
        policy.setPolicyId(policyId);
        policy.setActive(true);
        policy.setDefault(false);
        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());
        policy.setAccrualStartDate(LocalDate.now());

        policy = timeOffPolicyAdapter.savePolicy(policy);

        Set<String> finalUserSet = getFinalUserSet(request.getUserIds(), request.getGroupIds());
        if (!finalUserSet.isEmpty()) {
            if (policy.getAccrualType() == AccrualType.FIXED ) {
                if (request.getUserValidFrom() == null || request.getUserValidTo() == null) {
                    throw new IllegalArgumentException("userValidFrom and userValidTo are required for FIXED accrual");
                }
            }
            validateUserValidDates(request.getUserValidFrom(), request.getUserValidTo(),
                    request.getValidityStartDate(), request.getValidityEndDate());
        }

        if (finalUserSet.isEmpty())
            return timeOffPolicyEntityMapper.toResponseModel(policy);

        boolean hasUsers = !finalUserSet.isEmpty();

        if (hasUsers && request.getUserValidFrom() == null) {
            throw new IllegalArgumentException("userValidFrom is required when assigning users.");
        }

        if (hasUsers && accrual == AccrualType.FIXED) {
            if (request.getUserValidTo() == null) {
                throw new IllegalArgumentException("For FIXED policies,both userFrom and  userValidTo is required");
            }
        }
        List<String> finalUsers = new ArrayList<>(finalUserSet);

        LocalDate from = request.getUserValidFrom();
        LocalDate to = request.getUserValidTo();

        double totalUnits = (policy.getCompensation() != Compensation.UNPAID)
                ? calculateTotalUnits(policy, request.getEntitledType())
                : 0.0;

        List<UserPolicyEntity> assignList = new ArrayList<>();
        List<LeaveBalanceEntity> balanceList = new ArrayList<>();

        for (String userId : finalUsers) {

            UserEntity userEntity = userAdapter.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

            List<UserPolicyEntity> existingActive =
                    userPolicyAdapter.findActivePoliciesByUserId(userId);

            UserPolicyEntity overrideUP = null;

            for (UserPolicyEntity up : existingActive) {

                AccrualType oldAcc = up.getPolicy().getAccrualType();
                EntitledType oldEnt = up.getPolicy().getEntitledType();

                if (oldAcc == AccrualType.FIXED && policy.getAccrualType() == AccrualType.FIXED) {
                    overrideUP = up;
                    break;
                }

                if (up.getPolicy().getCompensation() == Compensation.UNPAID &&
                        policy.getCompensation() == Compensation.UNPAID) {
                    overrideUP = up;
                    break;
                }

                if (oldAcc == policy.getAccrualType() &&
                        oldEnt == policy.getEntitledType()) {

                    overrideUP = up;
                    break;
                }

                if ((oldAcc == AccrualType.MONTHLY && policy.getAccrualType() == AccrualType.ANNUALLY) ||
                        (oldAcc == AccrualType.ANNUALLY && policy.getAccrualType() == AccrualType.MONTHLY)) {

                    overrideUP = up;
                    break;
                }
            }

            LeaveBalanceEntity oldLB = null;

            if (overrideUP != null) {

                overrideUP.setActive(false);
                overrideUP.setValidTo(LocalDate.now());
                userPolicyAdapter.saveUserPolicy(overrideUP);

                oldLB = leaveBalanceAdapter.findActiveBalanceByUserIdAndPolicy(
                        userId, overrideUP.getPolicy().getPolicyId());

                if (oldLB != null) {
                    oldLB.setActive(false);
                    oldLB.setUpdatedAt(LocalDateTime.now());
                    leaveBalanceAdapter.saveLeaveBalance(oldLB);
                }
            }

            LeaveBalanceEntity newLB = null;

            if (policy.getCompensation() != Compensation.UNPAID) {

                double taken = (oldLB != null) ? oldLB.getLeaveTakenUnits() : 0.0;
                double balance = Math.max(totalUnits - taken, 0);

                newLB = buildLeaveBalance(policy, userId, from, to, totalUnits);
                newLB.setLeaveTakenUnits(taken);
                newLB.setBalanceUnits(balance);
                newLB.setActive(true);

                balanceList.add(newLB);
            }

            UserPolicyEntity newUP = buildUserPolicy(policy, userEntity, from, to);
            newUP.setActive(true);

            assignList.add(newUP);
        }

        if (!assignList.isEmpty()) userPolicyAdapter.saveUserPolicies(assignList);
        if (!balanceList.isEmpty()) leaveBalanceAdapter.saveLeaveBalances(balanceList);

        return timeOffPolicyEntityMapper.toResponseModel(policy);
    }


    @Override
    public List<EnumModel> getDropDowns() {
        List<EnumModel> list=new ArrayList<>();
        for(EntitledType e:EntitledType.values()){
            EnumModel model= new EnumModel(e.name(),e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    @Transactional
    public void editPolicy(TimeOffPolicyEditRequestModel request) {

        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findByPolicyId(request.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("Invalid Policy ID");
        }

        if (policy.getPolicyName().equalsIgnoreCase("Custom Policy") ||
                policy.getPolicyId().equalsIgnoreCase("TOP00001")) {
            throw new IllegalArgumentException("Custom Policy cannot be edited");
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = request.getValidityEndDate();

        if (policy.getAccrualType() == AccrualType.FIXED) {
            if (Boolean.TRUE.equals(request.getCarryForward())) {
                throw new IllegalArgumentException("For Fixed accrual type, carry forward is not allowed");
            }
        }

        if (request.getMaxCarryForwardUnits() != null && request.getEntitledUnits() != null) {
            if (request.getMaxCarryForwardUnits() > request.getEntitledUnits()) {
                throw new IllegalArgumentException("Max carry-forward must be <= entitled units");
            }
        }

        if (Boolean.TRUE.equals(request.getCarryForward()) &&
                request.getMaxCarryForwardUnits() == null) {
            throw new IllegalArgumentException("maxCarryForwardUnits is required when carryForward is true");
        }

        if (policy.getEntitledType() != EntitledType.DAY &&
                (Boolean.TRUE.equals(request.getCarryForward()) || request.getMaxCarryForwardUnits() != null)) {
            throw new IllegalArgumentException("Carry forward is allowed only for DAY entitlement type");
        }

        boolean entitlementChanged = false;

        if (request.getPolicyName() != null && !request.getPolicyName().trim().isEmpty()) {
            policy.setPolicyName(request.getPolicyName().trim());
        }

        if (request.getEntitledUnits() != null) {
            policy.setEntitledUnits(request.getEntitledUnits());
            entitlementChanged = true;
        }

        if (request.getCarryForward() != null) {
            policy.setCarryForward(request.getCarryForward());
            policy.setMaxCarryForwardUnits(request.getMaxCarryForwardUnits());
        }

        if (request.getValidityEndDate() != null) {
            policy.setValidityEndDate(request.getValidityEndDate());
        }

        policy.setUpdatedAt(LocalDateTime.now());
        timeOffPolicyAdapter.savePolicy(policy);

        List<UserPolicyEntity> assignedUsers =
                userPolicyAdapter.findUserPoliciesByPolicyId(policy.getPolicyId());

        List<LeaveBalanceEntity> assignedBalances =
                leaveBalanceAdapter.findLeaveBalancesByPolicyId(policy.getPolicyId());

         double carryForwardUnits =
                    (policy.getMaxCarryForwardUnits() == null ? 0 : policy.getMaxCarryForwardUnits());

            for (LeaveBalanceEntity lb : assignedBalances) {
                lb.setCarryForwardUnits(carryForwardUnits);
                lb.setUpdatedAt(LocalDateTime.now());
            }

            leaveBalanceAdapter.saveLeaveBalances(assignedBalances);

        if (!entitlementChanged || policy.getCompensation() == Compensation.UNPAID) {
            return;
        }

        List<LeaveBalanceEntity> leaveBalances =
                leaveBalanceAdapter.findLeaveBalancesByPolicyId(policy.getPolicyId());

        for (LeaveBalanceEntity lb : leaveBalances) {

            if (request.getEntitledUnits() != null) {
                double total = request.getEntitledUnits();
                lb.setTotalUnits(total);

                double taken = lb.getLeaveTakenUnits();
                double newBalance = total - taken;
                if (newBalance < 0) newBalance = 0;

                lb.setBalanceUnits(newBalance);
            }

            lb.setUpdatedAt(LocalDateTime.now());
        }

        leaveBalanceAdapter.saveLeaveBalances(leaveBalances);
    }


    @Override
    @Transactional
    public void assignPolicies(TimeOffPolicyBulkAssignModel request) {

        Set<String> finalUsers = getFinalUserSet(request.getUserIds(), request.getGroupIds());
        if (finalUsers.isEmpty() ||
                request.getPolicyId() == null ||
                request.getPolicyId().trim().isEmpty()) {
            return;
        }

        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findByPolicyId(request.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("Invalid policyId: " + request.getPolicyId());
        }

        LocalDate userFrom = request.getUserValidFrom();
        LocalDate userTo = request.getUserValidTo();

        if (userFrom == null) {
            throw new IllegalArgumentException("User Valid From is required");
        }

        validateUserValidDates(
                userFrom, userTo,
                policy.getValidityStartDate(),
                policy.getValidityEndDate()
        );

        if (policy.getAccrualType() == AccrualType.FIXED && userTo == null) {
            throw new IllegalArgumentException("For FIXED policies userTo is required");
        }

        List<UserEntity> allUsers = userAdapter.findByUserId(new ArrayList<>(finalUsers));
        Map<String, UserEntity> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserEntity::getUserId, u -> u));

        List<UserPolicyEntity> assignList = new ArrayList<>();
        List<LeaveBalanceEntity> balanceList = new ArrayList<>();

        Double totalUnits = (policy.getCompensation() != Compensation.UNPAID)
                ? policy.getEntitledUnits().doubleValue()
                : null;


        LocalDate from = request.getUserValidFrom();
        LocalDate to = request.getUserValidTo();
        for (String userId : finalUsers) {

            UserEntity userEntity = userAdapter.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

            List<UserPolicyEntity> existingActive =
                    userPolicyAdapter.findActivePoliciesByUserId(userId);

            boolean alreadyHasSamePolicy = existingActive.stream()
                    .anyMatch(up -> up.getPolicy().getPolicyId().equals(policy.getPolicyId()));

            if (alreadyHasSamePolicy) {
                continue;
            }
            UserPolicyEntity overrideUP = null;

            for (UserPolicyEntity up : existingActive) {

                AccrualType oldAcc = up.getPolicy().getAccrualType();
                EntitledType oldEnt = up.getPolicy().getEntitledType();

                if (oldAcc == AccrualType.FIXED && policy.getAccrualType() == AccrualType.FIXED) {
                    overrideUP = up;
                    break;
                }

                if (up.getPolicy().getCompensation() == Compensation.UNPAID &&
                        policy.getCompensation() == Compensation.UNPAID) {
                    overrideUP = up;
                    break;
                }

                if (oldAcc == policy.getAccrualType() &&
                        oldEnt == policy.getEntitledType()) {

                    overrideUP = up;
                    break;
                }

                if ((oldAcc == AccrualType.MONTHLY && policy.getAccrualType() == AccrualType.ANNUALLY) ||
                        (oldAcc == AccrualType.ANNUALLY && policy.getAccrualType() == AccrualType.MONTHLY)) {

                    overrideUP = up;
                    break;
                }
            }

            LeaveBalanceEntity oldLB = null;

            if (overrideUP != null) {

                overrideUP.setActive(false);
                overrideUP.setValidTo(LocalDate.now());
                userPolicyAdapter.saveUserPolicy(overrideUP);

                oldLB = leaveBalanceAdapter.findActiveBalanceByUserIdAndPolicy(
                        userId, overrideUP.getPolicy().getPolicyId());

                if (oldLB != null) {
                    oldLB.setActive(false);
                    oldLB.setUpdatedAt(LocalDateTime.now());
                    leaveBalanceAdapter.saveLeaveBalance(oldLB);
                }
            }

            LeaveBalanceEntity newLB = null;

            if (policy.getCompensation() != Compensation.UNPAID) {

                double taken = (oldLB != null) ? oldLB.getLeaveTakenUnits() : 0.0;
                double balance = Math.max(totalUnits - taken, 0);

                newLB = buildLeaveBalance(policy, userId, from, to, totalUnits);
                newLB.setLeaveTakenUnits(taken);
                newLB.setBalanceUnits(balance);
                newLB.setActive(true);

                balanceList.add(newLB);
            }

            UserPolicyEntity newUP = buildUserPolicy(policy, userEntity, from, to);
            newUP.setActive(true);

            assignList.add(newUP);
        }


        if (!assignList.isEmpty()) userPolicyAdapter.saveUserPolicies(assignList);
        if (!balanceList.isEmpty()) leaveBalanceAdapter.saveLeaveBalances(balanceList);
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

        if (!status) {
            List<UserPolicyEntity> assigedUp = userPolicyAdapter.findUserPoliciesByPolicyId(policyId);

            for (UserPolicyEntity userPolicy : assigedUp) {
                userPolicy.setActive(status);
            }

            List<LeaveBalanceEntity> assignedLb = leaveBalanceAdapter.findLeaveBalancesByPolicyId(policyId);
            for (LeaveBalanceEntity leaveBalance : assignedLb) {
                leaveBalance.setActive(status);
                leaveBalance.setUpdatedAt(LocalDateTime.now());
            }
            if (!assigedUp.isEmpty()) {
                userPolicyAdapter.saveUserPolicies(assigedUp);
            }
            if (!assignedLb.isEmpty()) {
                leaveBalanceAdapter.saveLeaveBalances(assignedLb);
            }
        }

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
    public List<EnumModel> getAccrualTypeStatus() {
        List<EnumModel> list = new ArrayList<>();
        for (AccrualType e : AccrualType.values()) {
            EnumModel model = new EnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public List<EnumModel> getCompensation() {
        List<EnumModel> list=new ArrayList<>();
        for(Compensation e:Compensation.values()){
            EnumModel model= new EnumModel(e.name(),e.getValue());
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

        LocalDate computedValidTo = computeValidTo(policy, validFrom, validTo);

        lb.setPeriodStartDate(validFrom);
        lb.setPeriodEnd(computedValidTo);

        lb.setTotalUnits(totalUnits);
        lb.setBalanceUnits(totalUnits);
        lb.setLeaveTakenUnits(0.0);

        double carryForwardUnits=policy.getMaxCarryForwardUnits() == null ?
                0 : policy.getMaxCarryForwardUnits();
        lb.setCarryForwardUnits(carryForwardUnits);

        lb.setLastAccrualDate(validFrom);

        LocalDate nextAccrualDate = resolveNextAccrualDate(
                validFrom, validTo, policy.getValidityEndDate(), policy.getAccrualType());
        lb.setNextAccrualDate(nextAccrualDate);
        lb.setActive(true);
        lb.setCreatedAt(LocalDateTime.now());
        lb.setUpdatedAt(LocalDateTime.now());

        return lb;
    }

    private UserPolicyEntity buildUserPolicy(TimeOffPolicyEntity policy, UserEntity user, LocalDate validFrom, LocalDate validTo) {


        UserPolicyEntity up = new UserPolicyEntity();
        up.setUser(user);
        up.setPolicy(policy);
        up.setValidFrom(validFrom);
        up.setActive(true);
        LocalDate computedValidTo = validTo == null ? policy.getValidityEndDate() : validTo ;
        up.setValidTo(computedValidTo);

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

    private void validateUserPolicyRules(List<UserPolicyEntity> existingAssignments,
                                         AccrualType newAccrual,
                                         EntitledType newEntitled) {

        for (UserPolicyEntity up : existingAssignments) {

            AccrualType oldAccrual = up.getPolicy().getAccrualType();

            if (oldAccrual == AccrualType.FIXED || newAccrual == AccrualType.FIXED) {
                throw new IllegalArgumentException(
                        "User " + up.getUser().getUserName() +
                                " already has FIXED accrual policy " + up.getPolicy().getPolicyName());
            }
        }
    }



    @Override
    public List<EnumModel> getResetFrequencyStatus() {
        List<EnumModel> list = new ArrayList<>();
        for (ResetFrequency e : ResetFrequency.values()) {
            EnumModel model = new EnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    private LocalDate computeValidTo(TimeOffPolicyEntity policy, LocalDate validFrom, LocalDate validTo) {

        if (validTo != null) {
            return validTo;
        }

        if (policy.getAccrualType() == AccrualType.MONTHLY) {
            return validFrom.withDayOfMonth(validFrom.lengthOfMonth());
        }

        if (policy.getAccrualType() == AccrualType.ANNUALLY) {
            return LocalDate.of(validFrom.getYear(), 12, 31);
        }
        return null;
    }
}
