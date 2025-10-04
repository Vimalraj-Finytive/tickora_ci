package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

public class SubscriptionDto {
    private String currentPlan;
    private String start;
    private String activeUntil;
    private String status;
    private String nextInvoiceDate;

    public String getCurrentPlan() {
        return currentPlan;
    }

    public void setCurrentPlan(String currentPlan) {
        this.currentPlan = currentPlan;
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
