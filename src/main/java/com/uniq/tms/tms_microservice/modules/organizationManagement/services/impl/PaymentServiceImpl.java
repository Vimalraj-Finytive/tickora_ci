package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.PaymentAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final PaymentAdapter paymentAdapter;

    public PaymentServiceImpl(PaymentAdapter paymentAdapter) {
        this.paymentAdapter = paymentAdapter;
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
    public PaymentEntity createPayment(String orgId,String subId, String billingCycle ,BigDecimal subscriptionAmount ,Integer subscribedUserCount, String planId, String orgSchema) {
        return paymentAdapter.createPayment(orgId,subId,billingCycle,subscriptionAmount, subscribedUserCount,planId,orgSchema);
    }


}
