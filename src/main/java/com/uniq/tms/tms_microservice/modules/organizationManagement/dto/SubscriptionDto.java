package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

public class SubscriptionDto {
    private String subscriptionId;
    private String planName;
    private String start;
    private String activeUntil;
    private String status;
    private String nextInvoiceDate;
    private String billingCycle;
    private Integer subscribedUsers;

    public Integer getSubscribedUsers() { return subscribedUsers; }

    public void setSubscribedUsers(Integer subscribedUsers) { this.subscribedUsers = subscribedUsers; }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getActiveUntil() {
        return activeUntil;
    }

    public void setActiveUntil(String activeUntil) {
        this.activeUntil = activeUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNextInvoiceDate() {
        return nextInvoiceDate;
    }

    public void setNextInvoiceDate(String nextInvoiceDate) {
        this.nextInvoiceDate = nextInvoiceDate;
    }
}
