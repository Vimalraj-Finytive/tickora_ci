package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

import java.time.LocalDateTime;

public class Subscription {
    private String subscriptionId;
    private String planName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String nextInvoiceAmount;
    private LocalDateTime nextInvoiceDate;
    private Integer subscribedUsers;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNextInvoiceAmount() {
        return nextInvoiceAmount;
    }

    public void setNextInvoiceAmount(String nextInvoiceAmount) {
        this.nextInvoiceAmount = nextInvoiceAmount;
    }

    public LocalDateTime getNextInvoiceDate() {
        return nextInvoiceDate;
    }

    public void setNextInvoiceDate(LocalDateTime nextInvoiceDate) {
        this.nextInvoiceDate = nextInvoiceDate;
    }

    public Integer getSubscribedUsers() {
        return subscribedUsers;
    }

    public void setSubscribedUsers(Integer subscribedUsers) {
        this.subscribedUsers = subscribedUsers;
    }
}
