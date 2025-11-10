package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.PaymentAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionMappingAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDetailsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionMappingEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.SubscriptionEntityMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Subscription;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.SubscriptionMappingRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.MonthlyPaymentModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.TopCustomersModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import com.uniq.tms.tms_microservice.shared.helper.InvoiceGeneratorHelper;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final PaymentAdapter paymentAdapter;
    private final SubscriptionAdapter subscriptionAdapter;
    private final OrganizationAdapter organizationAdapter;
    private final SubscriptionEntityMapper subscriptionEntityMapper;
    private final SubscriptionMappingAdapter subscriptionMappingAdapter;

    public PaymentServiceImpl(PaymentAdapter paymentAdapter, SubscriptionAdapter subscriptionAdapter, OrganizationAdapter organizationAdapter, SubscriptionEntityMapper subscriptionEntityMapper, SubscriptionMappingAdapter subscriptionMappingAdapter) {
        this.paymentAdapter = paymentAdapter;
        this.subscriptionAdapter = subscriptionAdapter;
        this.organizationAdapter = organizationAdapter;
        this.subscriptionEntityMapper = subscriptionEntityMapper;
        this.subscriptionMappingAdapter = subscriptionMappingAdapter;
    }

    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

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

        SubscriptionEntity subscription = subscriptionAdapter
                .findSubscriptionDetails(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        List<SubscriptionMappingEntity> mappings =
                subscriptionMappingAdapter.findBySubscriptionId(subscription.getSubId());

        if (mappings.isEmpty()) {
            throw new IllegalArgumentException("The Basic Plan is a free trial and does not have any payment details.");
        }

        paymentAdapter.getPaymentById(mappings.getFirst().getPaymentId(),subscription.getPlanId());

        if (mappings.isEmpty()) {
            throw new RuntimeException("No payments found for subscription: " + subscriptionId);
        }

        List<PaymentDetailsDto> paymentDetailsList = new ArrayList<>();

        String billingCycle = null;

        for (SubscriptionMappingEntity mapping : mappings) {

            PaymentEntity paymentEntity = paymentAdapter.getPaymentById(mapping.getPaymentId());

            if (billingCycle == null) {
                billingCycle = paymentEntity.getBillingPeriod();
            }

            Payment payment = null;
            try {
                RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
                payment = client.payments.fetch(paymentEntity.getOrderId());
            } catch (RazorpayException e) {
                log.warn("Razorpay fetch failed → fallback to DB for Payment ID: {}", paymentEntity.getPaymentId());
            }

            JSONObject p = payment != null ? payment.toJson() : new JSONObject();

            PaymentDetailsDto details = new PaymentDetailsDto();
            details.setPaymentStatus(p.optString("status", paymentEntity.getPaymentStatus()));
            details.setPaidAt(p.has("created_at") ? convertEpochToDate(p.getLong("created_at")) : paymentEntity.getPaymentDate().toString());
            details.setInvoiceId(p.optString("invoice_id", null));
            details.setAmount(p.has("amount") ? p.getInt("amount") / 100.0 : paymentEntity.getAmount().doubleValue());
            details.setMethod(p.optString("method", null));
            details.setPaymentId(paymentEntity.getPaymentId());

            paymentDetailsList.add(details);
        }

        Subscription subscriptionModel = subscriptionEntityMapper.toModel(subscription);

        PlanEntity plan = subscriptionAdapter.findById(subscriptionModel.getPlanName())
                .orElseThrow(() -> new RuntimeException("Plan not found for id: " + subscriptionModel.getPlanName()));

        PaymentDto dto = new PaymentDto();
        dto.setPlanName(plan.getPlanName());
        dto.setStatus(subscriptionModel.getStatus());
        dto.setStart(subscriptionModel.getStartDate().toLocalDate().toString());
        dto.setEnd(subscriptionModel.getEndDate().toLocalDate().toString());
        dto.setSubscribedUser(subscriptionModel.getSubscribedUsers());
        dto.setBillingCycle(billingCycle);
        dto.setPayments(paymentDetailsList);

        return dto;
    }



    private String convertEpochToDate(Long epochSeconds) {
        if (epochSeconds == null) return null;
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return zonedDateTime.format(formatter);
    }

   @Override
    public ResponseEntity<byte[]> getPaymentDetailsPdfBySubscriptionId(String subscriptionId, String orgId) {
        PaymentDto paymentDetails = getPaymentDetailsBySubscriptionId(subscriptionId);
        String OrgName =organizationAdapter.getOrgName(orgId);
        if (paymentDetails == null) {
            return ResponseEntity.noContent().build();
        }
        try {
            ByteArrayOutputStream pdfStream = InvoiceGeneratorHelper.generateInvoicePdf(paymentDetails, OrgName);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice.pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to generate PDF: " + e.getMessage()).getBytes());
        }
    }


    @Override
    public List<MonthlyPaymentModel> getOrganizationSales(int year) {
        try{
            Map<String, BigDecimal> monthlyTotalMap = Stream.of(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
            ).collect(Collectors.toMap(
                    m -> m,
                    m -> BigDecimal.ZERO,
                    (a, b) -> a,
                    LinkedHashMap::new
            ));
            List<OrganizationEntity> organizations = organizationAdapter.findAll();
            for (OrganizationEntity org : organizations) {
                try{
                    TenantUtil.clearTenant();
                    TenantUtil.setCurrentTenant(org.getSchemaName());
                    entityManager.clear();
                    List<Object[]> results = paymentAdapter.getMonthlyAmountWithShortMonthName(year);

                    for (Object[] row : results) {
                        String month = (String) row[0];
                        BigDecimal amount = (BigDecimal) row[1];
                        monthlyTotalMap.put(month, monthlyTotalMap.get(month).add(amount));
                    }

                } catch (Exception e) {
                    continue;
                }
            }

            List<MonthlyPaymentModel> result = new ArrayList<>();

            for (Map.Entry<String, BigDecimal> entry : monthlyTotalMap.entrySet()) {
                result.add(new MonthlyPaymentModel(entry.getKey()+" ,"+year, entry.getValue()));
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<TopCustomersModel> getOrganizationTopCustomers(int year) {
        List<OrganizationEntity> organizations = organizationAdapter.findAll();
        Map<String, BigDecimal> orgAmountMap = new HashMap<>();
        for (OrganizationEntity org : organizations) {
            try {
                TenantUtil.clearTenant();
                TenantUtil.setCurrentTenant(org.getSchemaName());
                BigDecimal amount = paymentAdapter.getTotalAmountByYear(year);
                orgAmountMap.put(org.getOrgName(), amount);
            } catch (Exception e) {
                continue;
            }
        }
        return orgAmountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new TopCustomersModel(entry.getKey(), entry.getValue()))
                .toList();
    }
}
