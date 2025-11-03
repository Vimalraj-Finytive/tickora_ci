package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.impl;


import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.OrganizationStatusEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.PlanDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.PlanEntityMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.SubscriptionDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.SubscriptionEntityMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Plan;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.PaymentRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.PlanRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.SubscriptionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class SubscriptionAdapterImpl implements SubscriptionAdapter {
    private final PaymentRepository paymentRepository;
    private static final Logger log = LogManager.getLogger(SubscriptionAdapterImpl.class);
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final SubscriptionEntityMapper subscriptionEntityMapper;
    private final SubscriptionDtoMapper subscriptionDtoMapper;
    private final PlanEntityMapper planEntityMapper;
    private final PlanDtoMapper planDtoMapper;
    private final IdGenerationService idGenerationService;
    private final OrganizationRepository organizationRepository;

    public SubscriptionAdapterImpl(PaymentRepository paymentRepository, SubscriptionRepository subscriptionRepository, SubscriptionEntityMapper subscriptionEntityMapper, SubscriptionDtoMapper subscriptionDtoMapper, PlanRepository planRepository, PlanEntityMapper planEntityMapper, PlanDtoMapper planDtoMapper, IdGenerationService idGenerationService, OrganizationRepository organizationRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionEntityMapper = subscriptionEntityMapper;
        this.subscriptionDtoMapper = subscriptionDtoMapper;
        this.planRepository = planRepository;
        this.planEntityMapper = planEntityMapper;
        this.planDtoMapper = planDtoMapper;
        this.idGenerationService = idGenerationService;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public long countSubscribedUsers(String orgId) {
        return subscriptionRepository.getSubscribedUsersByOrgId(orgId).orElse(0L);
    }

    @Override
    public SubscriptionDto getActivePlan(String orgId) {
        return subscriptionRepository.findActiveSubscription(orgId)
                .map(entity -> {
                    LocalDateTime now = LocalDateTime.now();
                    if (entity.getEndDate().isBefore(now) || entity.getEndDate().isEqual(now)) {
                        entity.setStatus(OrganizationStatusEnum.EXPIRED.getDisplayValue());
                        entity.setUpdatedAt(now);
                        subscriptionRepository.save(entity);
                    } else {
                        log.info("Subscription still active. No status change required.");
                    }
                    return entity;
                })
                .map(subscriptionEntityMapper::toModel)
                .map(model -> {
                    log.info("Mapped SubscriptionEntity to model: {}", model);
                    SubscriptionDto dto = subscriptionDtoMapper.toDto(model);
                    Optional<PlanEntity> planOpt = planRepository.findById(model.getPlanName());
                    planOpt.ifPresent(plan -> {
                        dto.setPlanName(plan.getPlanName());
                        dto.setBillingCycle(plan.getBillingCycle());
                    });
                    dto.setStatus(model.getStatus());
                    return dto;
                })
                .orElseGet(() -> {
                    log.warn("No active subscription found for orgId: {}", orgId);
                    return null;
                });
    }


    @Override
    public List<SubscriptionDto> getPlanHistory(String orgId) {
        return subscriptionRepository.findAllSubscriptionsByOrgId(orgId)
                .stream()
                .map(subscriptionEntityMapper::toModel)
                .map(model -> {
                    SubscriptionDto dto = subscriptionDtoMapper.toDto(model); // Model → DTO
                    Optional<PlanEntity> planOpt = planRepository.findById(model.getPlanName());
                    planOpt.ifPresent(plan -> {
                        dto.setPlanName(plan.getPlanName());
                        dto.setBillingCycle(plan.getBillingCycle());
                    });
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<PlanDto> getAllPlans() {
        List<PlanEntity> entities = planRepository.findAllPlans();
        List<Plan> plans = planEntityMapper.toModelList(entities);
        return planDtoMapper.toDtoList(plans);
    }


    @Override
    public boolean validatePlanAmount(String planId, Integer subscribedUserCount, BigDecimal totalSubscriptionAmount) {
        PlanEntity plan = planRepository.findByPlanId(planId)
                .orElseThrow(() -> new RuntimeException("Invalid Plan ID"));
        int months = convertBillingCycleToMonths(plan.getBillingCycle());
        BigDecimal expectedAmount = plan.getPricePerUser()
                .multiply(BigDecimal.valueOf(subscribedUserCount))
                .multiply(BigDecimal.valueOf(months));
        log.info("- Expected Amount (calculated): {}", expectedAmount);
        log.info("- Requested Amount: {}", totalSubscriptionAmount);
        return expectedAmount.compareTo(totalSubscriptionAmount) == 0;
    }

    @Override
    public PaymentStatus  updatePlan(String orgId,String orgSchema, String planId, Integer subscribedUserCount,String paymentId) {
        LocalDateTime now = LocalDateTime.now();
        boolean wasUpdated = subscriptionRepository.findActiveSubscription(orgId)
                .map(entity -> {
                    boolean updated = false;
                    if (!OrganizationStatusEnum.EXPIRED.getDisplayValue().equals(entity.getStatus())) {
                        entity.setStatus(OrganizationStatusEnum.SUSPENDED.getDisplayValue());
                        entity.setUpdatedAt(now);
                        subscriptionRepository.save(entity);
                        updated = true;
                    }
                    return updated;
                })
                .orElse(false);
        PlanEntity plan = planRepository.findByPlanId(planId)
                .orElseThrow(() -> new RuntimeException("Invalid Plan ID"));
        int months = convertBillingCycleToMonths(plan.getBillingCycle());
        int durationInDays = months * 30;
        String newSubId = idGenerationService.generateNextSubscriptionId(orgId);
        try {
            SubscriptionEntity newSubscription = new SubscriptionEntity();
            newSubscription.setSubId(newSubId);
            newSubscription.setOrgId(orgId);
            newSubscription.setPlanId(planId);
            newSubscription.setSchemaName(orgSchema);
            newSubscription.setStatus(OrganizationStatusEnum.ACTIVE.getDisplayValue());
            newSubscription.setStartDate(now);
            newSubscription.setEndDate(now.plusDays(durationInDays));
            newSubscription.setSubscribedUsers(subscribedUserCount);
            newSubscription.setPaymentId(paymentId);

            log.info("Creating new subscription for Organization ID: {}", orgId);
            subscriptionRepository.save(newSubscription);
            return PaymentStatus.SUCCESS;
        } catch (Exception e) {
            log.error("Failed to create new subscription for Organization ID: {}", orgId, e);
            return PaymentStatus.FAILED;
        }
    }

    @Override
    public String getBillingCycle(String planId) {
        PlanEntity plan = planRepository.findByPlanId(planId)
                .orElseThrow(() -> new RuntimeException("Invalid Plan ID"));
        return plan.getBillingCycle();
    }


    private int convertBillingCycleToMonths(String billingCycle) {
        if (billingCycle == null || billingCycle.isBlank()) {
            return 1;
        }
        String[] parts = billingCycle.trim().split(" ");
        try {
            int months = Integer.parseInt(parts[0]);
            return months > 0 ? months : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public Optional<SubscriptionEntity> findActiveSubscriptionByOrgId(String orgId) {
        return subscriptionRepository.findActiveSubscriptionByOrgId(orgId);
    }
    public Optional<PlanEntity> findById(String planId) {
        return planRepository.findById(planId);
    }

    @Override
    public Optional<SubscriptionEntity> findSubscriptionDetails(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId);
    }

    @Override
    public List<SubscriptionEntity> findAllSubscriptionsByOrgId(String orgId) {
        return subscriptionRepository.findAllSubscriptionsByOrgId(orgId);
    }

    @Override
    public boolean updateSubscription(SubscriptionEntity subscription) {
        try {
            subscription.setUpdatedAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);
            return true;
        } catch (Exception e) {
            // You can add your logger here
            System.err.println("Failed to update subscription: " + e.getMessage());
            return false;
        }
    }


    @Override
    public List<SubscriptionEntity> getAllSubscriptionsBetweenDates(LocalDate fromDate, LocalDate toDate) {
        return subscriptionRepository.findAllByStartDateBetween(
                fromDate.atStartOfDay(),
                toDate.atTime(23, 59, 59)
        );
    }

    @Override
    public List<SubscriptionEntity> getAllSubscriptionsForOrgBetweenDates(LocalDate fromDate, LocalDate toDate) {
        return subscriptionRepository.findByStartDateBetween(fromDate.atStartOfDay(), toDate.atTime(23, 59, 59));
    }



}
