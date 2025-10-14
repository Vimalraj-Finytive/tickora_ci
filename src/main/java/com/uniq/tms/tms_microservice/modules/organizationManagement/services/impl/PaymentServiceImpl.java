package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.PaymentAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDetailsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PlaneName;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final PaymentAdapter paymentAdapter;
    private final SubscriptionAdapter subscriptionAdapter;

    public PaymentServiceImpl(PaymentAdapter paymentAdapter, SubscriptionAdapter subscriptionAdapter) {
        this.paymentAdapter = paymentAdapter;
        this.subscriptionAdapter = subscriptionAdapter;
    }

    @Override
    public String createPaymentOrder(String orgId, BigDecimal amount) {
        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            BigDecimal amountInPaise = amount.multiply(BigDecimal.valueOf(100));
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise.intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + orgId + "_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);
            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");
            String status = order.get("status");

            return String.format(
                    " Razorpay test order created successfully!" +
                            "Organization: %s" +
                            "Order ID: %s" +
                            "Amount: ₹%.2f" +
                            "Status: %s",
                    orgId, orderId, amount, status
            );

        } catch (Exception e) {
            e.printStackTrace();
            return String.format(
                    " Failed to create Razorpay test order for Org %s. Error: %s",
                    orgId, e.getMessage()
            );
        }
    }

    @Override
    public PaymentEntity createPayment(String orgId, String orderId,  BigDecimal amount,String billingCycle, String orgSchema, PaymentStatus status) {
        return paymentAdapter.createPayment(orgId,orderId,amount,billingCycle,status,orgSchema);
    }


    @Override
    public PaymentDto getPaymentDetailsBySubscriptionId(String subscriptionId) {
        PaymentDto paymentDto = new PaymentDto();

        try {
            SubscriptionEntity subscriptionEntity = subscriptionAdapter
                    .findSubscriptionDetails(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            PaymentEntity paymentEntity = paymentAdapter.getPaymentById(subscriptionEntity.getPaymentId());
            PlanEntity plan = subscriptionAdapter.findById(subscriptionEntity.getPlanId()).orElse(null);
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            Payment payment = null;
            try {
                payment = client.payments.fetch(paymentEntity.getOrderId());
            } catch (RazorpayException e) {
                if (e.getMessage().contains("BAD_REQUEST_ERROR")) {
                    // Payment ID not found, log and fallback to database values
                    log.warn("Payment ID not found in Razorpay: " + paymentEntity.getPaymentId());
                } else {
                    throw e;
                }
            }
            JSONObject p = payment != null ? payment.toJson() : new JSONObject();

            PaymentDetailsDto paymentDetails = new PaymentDetailsDto();
            paymentDetails.setPaymentStatus(p.optString("status", paymentEntity.getPaymentStatus()));
            paymentDetails.setPaidAt(p.has("created_at") ? convertEpochToDate(p.getLong("created_at")) : paymentEntity.getPaymentDate().toString());
            paymentDetails.setInvoiceId(p.optString("invoice_id", null));
            paymentDetails.setCreatedAt(p.has("created_at") ? p.getLong("created_at") : null);
            paymentDetails.setBank(p.optString("bank", null));
            paymentDetails.setAmount(p.has("amount") ? p.getInt("amount") / 100.0 : paymentEntity.getAmount().doubleValue());
            paymentDetails.setMethod(p.optString("method", null));
            paymentDetails.setPaymentId(p.optString("id", paymentEntity.getPaymentId()));
            paymentDetails.setContact(p.optString("contact", null));
            paymentDetails.setCurrency(p.optString("currency", null));
            paymentDetails.setEmail(p.optString("email", null));
            paymentDetails.setStatus(p.optString("status", null));

            paymentDto.setStatus(subscriptionEntity.getStatus());
            paymentDto.setStart(subscriptionEntity.getStartDate().toLocalDate().toString());
            paymentDto.setEnd(subscriptionEntity.getEndDate().toLocalDate().toString());
            paymentDto.setSubscribedUser(subscriptionEntity.getSubscribedUsers());
            paymentDto.setBillingCycle(paymentEntity.getBillingPeriod());
            paymentDto.setPayment(paymentDetails);


            String planName = (plan != null) ? plan.getPlanName() : "Unknown Plan";
            paymentDto.setPlanName(planName);


        } catch (RazorpayException e) {
            throw new RuntimeException("Error fetching payment from Razorpay: " + e.getMessage());
        }

        return paymentDto;
    }

    private String convertEpochToDate(Long epochSeconds) {
        if (epochSeconds == null) return null;
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return zonedDateTime.format(formatter);
    }




}
