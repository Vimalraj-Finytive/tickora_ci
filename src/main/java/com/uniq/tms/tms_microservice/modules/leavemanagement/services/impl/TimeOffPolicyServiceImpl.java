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
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheKeyConfig;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.shared.util.CacheEventPublisherUtil;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.time.temporal.ChronoUnit;
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
    private final CacheKeyConfig cacheKeyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;
    private final ApplicationEventPublisher publisher;
    private final AuthHelper authHelper;

    public TimeOffPolicyServiceImpl(IdGenerationService idGenerationService, UserAdapter userAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper, UserPolicyAdapter userPolicyAdapter, LeaveBalanceAdapter leaveBalanceAdapter,
                                    TimeOffPolicyAdapter timeOffPolicyAdapter, CacheKeyConfig cacheKeyConfig, CacheReloadHandlerRegistry cacheReloadHandlerRegistry, ApplicationEventPublisher publisher, CacheKeyUtil cacheKeyUtil, AuthHelper authHelper) {
        this.idGenerationService = idGenerationService;
        this.userAdapter = userAdapter;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
        this.userPolicyAdapter = userPolicyAdapter;
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
        this.cacheKeyConfig = cacheKeyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
        this.publisher = publisher;
        this.authHelper = authHelper;
    }

    @Value("${cache.redis.enabled}")
    private boolean isRedisEnabled;

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
        if (request.getEntitledUnits() != null) {
            if (request.getEntitledUnits() < policy.getEntitledUnits()) {
                throw new IllegalArgumentException("Entitled units are not allowed to reduced");
            }
        }

        if (policy.getAccrualType() == AccrualType.FIXED) {
            if (Boolean.TRUE.equals(request.getCarryForward())) {
                throw new IllegalArgumentException("For Fixed accrual type, carry forward is not allowed");
            }
        }

        if (request.getMaxCarryForwardUnits() != null && request.getEntitledUnits() != null) {
            if (request.getMaxCarryForwardUnits() > request.getEntitledUnits()) {
                throw new IllegalArgumentException("Max carry-forward must be less than or equal to entitled units");
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


        if (request.getReschedule() != null){
            policy.setReschedule(request.getReschedule());
        }
        policy.setUpdatedAt(LocalDateTime.now());
        timeOffPolicyAdapter.savePolicy(policy);

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

        if (policy.getAccrualType() == AccrualType.FIXED){
            List<UserPolicyEntity> userPolicyEntities = userPolicyAdapter.findUserPoliciesByPolicyId(policy.getPolicyId());
            List<UserPolicyEntity> toUpdate =new ArrayList<>();
            for (UserPolicyEntity policyEntity: userPolicyEntities){
                policyEntity.setEntitledUnits(request.getEntitledUnits());
                toUpdate.add(policyEntity);
            }
            userPolicyAdapter.saveUserPolicies(toUpdate);
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
        String orgId = authHelper.getOrgId();
        String schema = authHelper.getSchema();
        Set<String> finalUsers = getFinalUserSet(request.getUserIds(), request.getGroupIds());
        if (finalUsers.isEmpty() ||
                request.getPolicyId() == null ||
                request.getPolicyId().trim().isEmpty()) {
            log.info("if  condition");
            return;
        }
        log.info("after if");

        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findByPolicyId(request.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("Invalid policyId: " + request.getPolicyId());
        }

        LocalDate userFrom = request.getUserValidFrom();
        LocalDate userTo = policy.getValidityEndDate();

        if (userFrom == null) {
            throw new IllegalArgumentException("User Valid From is required");
        }

        validateUserValidDates(
                userFrom, userTo,
                policy.getValidityStartDate(),
                policy.getValidityEndDate()
        );

        List<UserEntity> allUsers = userAdapter.findByUserId(new ArrayList<>(finalUsers));
        Map<String, UserEntity> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserEntity::getUserId, u -> u));

        List<UserPolicyEntity> assignList = new ArrayList<>();
        List<LeaveBalanceEntity> balanceList = new ArrayList<>();

        Double totalUnits = (policy.getCompensation() != Compensation.UNPAID)
                ? policy.getEntitledUnits().doubleValue()
                : null;

        LocalDate from = request.getUserValidFrom();
        LocalDate to = policy.getValidityEndDate();
        for (String userId : finalUsers) {

            UserEntity userEntity = userMap.get(userId);

            List<UserPolicyEntity> existingPolicies =
                    userPolicyAdapter.findUserPoliciesByUserId(userId);

            boolean alreadyHasPolicy = existingPolicies.stream()
                    .anyMatch(up -> up.getPolicy().getPolicyId().equals(policy.getPolicyId()));

            if (alreadyHasPolicy) {
                continue;
            }

            UserPolicyEntity newUP = buildUserPolicy(policy, userEntity, userFrom, userTo);
            newUP.setActive(true);
            assignList.add(newUP);
            if (policy.getCompensation() != Compensation.UNPAID) {
                LeaveBalanceEntity lb = buildLeaveBalance(
                        policy,
                        userId,
                        userFrom,
                        userTo,
                        totalUnits
                );
                lb.setLeaveTakenUnits(0.0);
                lb.setBalanceUnits(totalUnits);
                lb.setActive(true);
                balanceList.add(lb);
            }
        }
        if (!assignList.isEmpty()) userPolicyAdapter.saveUserPolicies(assignList);
        if (!balanceList.isEmpty()) leaveBalanceAdapter.saveLeaveBalances(balanceList);
        if (isRedisEnabled) {
            try {
                CacheEventPublisherUtil.syncReloadThenPublish(
                        publisher,
                        cacheKeyConfig.getUsers(),
                        orgId,
                        schema,
                        cacheReloadHandlerRegistry
                );
                log.info("User cache reload event published after assigned Policies to a user for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish User cache reload event for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload for orgId={}", orgId);
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

        if (!status) {
            List<UserPolicyEntity> assigedUp = userPolicyAdapter.findUserPoliciesByPolicyId(policyId);

            for (UserPolicyEntity userPolicy : assigedUp) {
                userPolicy.setActive(status);
            }

            List<LeaveBalanceEntity> assignedLb = leaveBalanceAdapter.findLeaveBalancesByPolicyId(policyId);
            for (LeaveBalanceEntity leaveBalance : assignedLb) {
                leaveBalance.setActive(false);
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
    public List<TimeOffPoliciesModel> getAllPolicy(String type) {
        try {
            List<TimeOffPolicyEntity> entities;
            if ("all".equalsIgnoreCase(type)) {
                entities = timeOffPolicyAdapter.findByIsActiveTrue();
            } else {
                entities = timeOffPolicyAdapter.findPoliciesList();
            }
            if (entities == null || entities.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No policies found");
            }
            return entities.stream()
                    .map(timeOffPolicyEntityMapper::toModel)
                    .collect(Collectors.toList());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch policies");
        }
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

        double calculatedUnits= calculateTotalUnits(policy,policy.getEntitledType());
        lb.setTotalUnits(calculatedUnits);
        lb.setBalanceUnits(totalUnits);
        lb.setLeaveTakenUnits(0.0);

        double carryForwardUnits=policy.getMaxCarryForwardUnits() == null ?
                0 : policy.getMaxCarryForwardUnits();
        lb.setCarryForwardUnits(carryForwardUnits);

        lb.setLastAccrualDate(validFrom);
        LocalDate nextAccrualDate = calculateNextAccrualDate(validFrom, policy.getAccrualType());
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
        up.setUpdatedAt(LocalDateTime.now());
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

    @Override
    public List<EnumModel> getResetFrequencyStatus() {
        List<EnumModel> list = new ArrayList<>();
        for (ResetFrequency e : ResetFrequency.values()) {
            EnumModel model = new EnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public void updateMonthlyPolicy() {
        List<TimeOffPolicyEntity> monthlyPolicies =
                timeOffPolicyAdapter.findAllPoliciesByType(AccrualType.MONTHLY);
        log.info("fetched policies");
        updatePolicy(monthlyPolicies, AccrualType.MONTHLY);
    }

    @Override
    public void updateYearlyPolicy() {
        List<TimeOffPolicyEntity> monthlyPolicies =
                timeOffPolicyAdapter.findAllPoliciesByType(AccrualType.ANNUALLY);
        updatePolicy(monthlyPolicies, AccrualType.ANNUALLY);
    }

    private void updatePolicy(List<TimeOffPolicyEntity> monthlyPolicies, AccrualType type){
        LocalDate current = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        for (TimeOffPolicyEntity entity : monthlyPolicies){
            log.info("loop start");
            if (entity.getReschedule() && entity.getValidityEndDate().isBefore(current)){
                LocalDate newStart;
                LocalDate newEnd;
                log.info("condition applied");
                TimeOffPolicyInactivateModel model = new TimeOffPolicyInactivateModel();
                List<String> userIds = userPolicyAdapter.findUserIdsByPolicyId(entity.getPolicyId());
                model.setActive(false);
                inactivatePolicy(entity.getPolicyId(), model);
                TimeOffPolicyEntity copy = new TimeOffPolicyEntity();
                String policyId = idGenerationService.generateNextTimeOffPolicyId();
                if (type == AccrualType.MONTHLY) {
                    long months = ChronoUnit.MONTHS.between(
                            entity.getValidityStartDate().withDayOfMonth(1),
                            entity.getValidityEndDate().withDayOfMonth(1)
                    ) + 1;
                    newStart = entity.getValidityStartDate().plusMonths(months);
                    newEnd = entity.getValidityEndDate().plusMonths(months);
                }else {
                    Period diff = Period.between(entity.getValidityStartDate(), entity.getValidityEndDate());
                    long years = diff.getYears() + 1;
                    newStart = entity.getValidityStartDate().plusYears(years);
                    newEnd = entity.getValidityEndDate().plusYears(years);
                }
                log.info("startDate and endDate");
                copy.setPolicyId(policyId);
                copy.setPolicyName(entity.getPolicyName());
                copy.setCompensation(entity.getCompensation());
                copy.setAccrualType(entity.getAccrualType());
                copy.setValidityStartDate(newStart);
                copy.setValidityEndDate(newEnd);
                copy.setAccrualStartDate(current);
                copy.setResetFrequency(entity.getResetFrequency());
                copy.setEntitledUnits(entity.getEntitledUnits());
                copy.setEntitledType(entity.getEntitledType());
                copy.setActive(entity.isActive());
                copy.setMaxCarryForwardUnits(entity.getMaxCarryForwardUnits());
                copy.setCarryForward(entity.getCarryForward());
                copy.setDefault(entity.getDefault());
                copy.setReschedule(entity.getReschedule());
                log.info("before save");
                TimeOffPolicyEntity saved = timeOffPolicyAdapter.savePolicy(copy);
                log.info("policy saved");

                TimeOffPolicyBulkAssignModel defaultPolicy = new TimeOffPolicyBulkAssignModel();
                defaultPolicy.setUserIds(userIds);
                defaultPolicy.setPolicyId(policyId);
                defaultPolicy.setUserValidFrom(newStart);
                assignPolicies(defaultPolicy);
            }
        }
    }

    private LocalDate computeValidTo(TimeOffPolicyEntity policy, LocalDate validFrom, LocalDate validTo) {

        if (policy.getAccrualType() == AccrualType.MONTHLY) {
            return validFrom.withDayOfMonth(validFrom.lengthOfMonth());
        }

        if (policy.getAccrualType() == AccrualType.ANNUALLY) {
            return LocalDate.of(validFrom.getYear(), 12, 31);
        }
        return validTo;
    }

    @Override
    @Transactional
    public void editUserPolicy(List<EditUserPolicyModel> reqList) {
        String orgId = authHelper.getOrgId();
        String schema = authHelper.getSchema();
        if (reqList == null || reqList.isEmpty()) {
            throw new IllegalArgumentException("Request is empty");
        }
        LocalDate validFrom = reqList.getFirst().getValidityStartDate();
        if (validFrom == null) {
            throw new IllegalArgumentException("Validity start date is required");
        }
        String userId = reqList.getFirst().getUserId();
        List<String> requestedPolicyIds = reqList.stream()
                .map(EditUserPolicyModel::getPolicyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UserPolicyEntity> existingPolicies =
                userPolicyAdapter.findUserPoliciesByUserId(userId);
        Map<String, LocalDate> policyValidityStartMap =
                timeOffPolicyAdapter.findPoliciesByIds(requestedPolicyIds)
                        .stream()
                        .collect(Collectors.toMap(
                                TimeOffPolicyEntity::getPolicyId,
                                TimeOffPolicyEntity::getValidityStartDate
                        ));
        Map<String, UserPolicyEntity> existingMap =
                existingPolicies.stream()
                        .collect(Collectors.toMap(up -> up.getPolicy().getPolicyId(), up -> up));

        List<UserPolicyEntity> toSaveUP = new ArrayList<>();
        List<LeaveBalanceEntity> toSaveLB = new ArrayList<>();

        for (EditUserPolicyModel req : reqList) {

            String policyId = req.getPolicyId();
            LocalDate existingDate = policyValidityStartMap.get(policyId);
            if (existingDate != null && validFrom.isBefore(existingDate)) {
                throw new IllegalArgumentException(
                        "Start date cannot be before the existing policy start date"
                );
            }
            if (existingMap.containsKey(policyId)) {
                UserPolicyEntity up = existingMap.get(policyId);
                up.setValidFrom(validFrom);
                up.setActive(true);
                up.setUpdatedAt(LocalDateTime.now());
                toSaveUP.add(up);
            } else {
                TimeOffPolicyBulkAssignModel assignReq = new TimeOffPolicyBulkAssignModel();
                assignReq.setPolicyId(policyId);
                assignReq.setUserIds(List.of(userId));
                assignReq.setUserValidFrom(validFrom);

                this.assignPolicies(assignReq);
            }
        }

        for (UserPolicyEntity existing : existingPolicies) {

            String policyId = existing.getPolicy().getPolicyId();
            if(existing.getPolicy().getDefault().equals(Boolean.TRUE)){
                continue;
            }
            if (!requestedPolicyIds.contains(policyId)) {
                existing.setActive(false);
                existing.setUpdatedAt(LocalDateTime.now());
                toSaveUP.add(existing);
                TimeOffPolicyEntity policy = existing.getPolicy();
                if (policy.getCompensation() != Compensation.UNPAID) {
                    LeaveBalanceEntity lb =
                            leaveBalanceAdapter.findActiveBalanceByUserIdAndPolicy(userId, policyId);
                    if (lb != null) {
                        lb.setActive(false);
                        lb.setUpdatedAt(LocalDateTime.now());
                        toSaveLB.add(lb);
                    }
                }
            }
        }

        if (!toSaveUP.isEmpty()) {
            userPolicyAdapter.saveUserPolicies(toSaveUP);
        }

        if (!toSaveLB.isEmpty()) {
            leaveBalanceAdapter.saveLeaveBalances(toSaveLB);
        }
        if (isRedisEnabled) {
            try {
                CacheEventPublisherUtil.syncReloadThenPublish(
                        publisher,
                        cacheKeyConfig.getUsers(),
                        orgId,
                        schema,
                        cacheReloadHandlerRegistry
                );
                log.info("User cache reload event published after Edited Policies fo a user for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish User cache reload event for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload for orgId={}", orgId);
        }
    }
}
