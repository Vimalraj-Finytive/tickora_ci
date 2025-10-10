package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;

import java.math.BigDecimal;

public interface PaymentService {
    String createPaymentOrder(String orgId, BigDecimal amount);
    PaymentEntity createPayment(String orgId,String newSubId ,String orderId,String billingCycle, BigDecimal subscriptionAmount, Integer subscribedUserCount, String planId, String orgSchema);
}
