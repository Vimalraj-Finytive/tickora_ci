package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

import java.time.LocalDateTime;

public class Subscription {
    private String planName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String nextInvoiceAmount;
    private LocalDateTime nextInvoiceDate;

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
}
