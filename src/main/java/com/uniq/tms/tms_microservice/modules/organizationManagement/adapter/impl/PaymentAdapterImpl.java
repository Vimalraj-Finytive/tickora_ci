package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.PaymentAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.PaymentRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl.SubscriptionServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class PaymentAdapterImpl implements PaymentAdapter {
    private static final Logger log = LogManager.getLogger(PaymentAdapterImpl.class);
    private final IdGenerationService idGenerationService;
    private final PaymentRepository paymentRepository;

    public PaymentAdapterImpl(IdGenerationService idGenerationService, PaymentRepository paymentRepository) {
        this.idGenerationService = idGenerationService;
        this.paymentRepository = paymentRepository;
    }


    @Override
    public PaymentEntity createPayment(String orgId, String orderId, BigDecimal amount, String billingCycle, PaymentStatus status, String orgSchema) {
        try {

            PaymentEntity payment = new PaymentEntity();
            payment.setPaymentId(idGenerationService.generateNextPaymentID(orgId));
            payment.setOrderId(orderId);
            payment.setAmount(amount);
            payment.setBillingPeriod(billingCycle);
            payment.setPaymentStatus(String.valueOf(status));
            payment.setPaymentDate(LocalDateTime.now());
            payment.setSchemaName(orgSchema);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Failed to create payment for selected plan", orgId, e);
            throw new RuntimeException("Error while creating payment record: " + e.getMessage());
        }
    }

    public PaymentEntity getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for ID: " + paymentId));
    }

}
