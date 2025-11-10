package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.util.List;

public class PaymentDto {
    private String planName;
    private String status;
    private String start;
    private String end;
    private Integer subscribedUser;
    private String billingCycle;
    private List<PaymentDetailsDto> payments;

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Integer getSubscribedUser() {
        return subscribedUser;
    }

    public void setSubscribedUser(Integer subscribedUser) {
        this.subscribedUser = subscribedUser;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public List<PaymentDetailsDto> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentDetailsDto> payments) {
        this.payments = payments;
    }
//    public PaymentDetailsDto getPayment() {
//        return payment;
//    }
//
//    public void setPayment(PaymentDetailsDto payment) {
//        this.payment = payment;
//}
}