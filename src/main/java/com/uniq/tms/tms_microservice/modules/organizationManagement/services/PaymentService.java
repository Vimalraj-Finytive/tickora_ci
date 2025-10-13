package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;

import java.math.BigDecimal;

public interface PaymentService {
    String createPaymentOrder(String orgId, BigDecimal amount);
    PaymentEntity createPayment(String orgId, String orderId,  BigDecimal amount,String billingCycle, String orgSchema, PaymentStatus status);
}
