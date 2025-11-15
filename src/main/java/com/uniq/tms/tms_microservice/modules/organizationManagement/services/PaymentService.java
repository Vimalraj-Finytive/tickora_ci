package com.uniq.tms.tms_microservice.modules.organizationManagement.services;


import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.MonthlyPaymentModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.TopCustomersModel;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    String createPaymentOrder(String orgId, BigDecimal amount);
    PaymentEntity createPayment(String orgId, String orderId,  BigDecimal amount,String billingCycle, String orgSchema, PaymentStatus status);
    PaymentDto getPaymentDetailsBySubscriptionId(String subscriptionId);
    ResponseEntity<byte[]> getPaymentDetailsPdfBySubscriptionId(String subscriptionId, String orgId);
    List<MonthlyPaymentModel> getOrganizationSales(int year);
    List<TopCustomersModel> getOrganizationTopCustomers(int year);
}
