package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.PaymentAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
    public PaymentEntity createPayment(String orgId, String orderId,  BigDecimal amount,String billingCycle, String orgSchema, PaymentStatus status) {
        return paymentAdapter.createPayment(orgId,orderId,amount,billingCycle,status,orgSchema);
    }

    @Override
    public List<Map<String, Object>> getPaymentDetailsByOrderId(String paymentId) {
        List<Map<String, Object>> paymentDetails = new ArrayList<>();

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            Payment payment = client.payments.fetch(paymentId);

            if (payment != null) {
                JSONObject p = payment.toJson();

                Map<String, Object> detail = new HashMap<>();
                detail.put("paymentId", p.isNull("id") ? null : p.getString("id"));
                detail.put("method", p.isNull("method") ? null : p.getString("method"));
                detail.put("email", p.isNull("email") ? null : p.getString("email"));
                detail.put("contact", p.isNull("contact") ? null : p.getString("contact"));
                detail.put("bank", p.isNull("bank") ? null : p.getString("bank"));
                detail.put("amount", p.isNull("amount") ? null : p.getInt("amount") / 100.0);
                detail.put("currency", p.isNull("currency") ? null : p.getString("currency"));
                detail.put("status", p.isNull("status") ? null : p.getString("status"));
                detail.put("createdAt", p.isNull("created_at") ? null : p.getLong("created_at"));

                paymentDetails.add(detail);
            }

        } catch (RazorpayException e) {
            System.err.println("Error fetching payment: " + e.getMessage());
        }

        return paymentDetails; // Always safe for Jackson
    }


}
