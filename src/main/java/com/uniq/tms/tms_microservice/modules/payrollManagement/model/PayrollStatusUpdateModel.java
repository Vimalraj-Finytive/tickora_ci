package com.uniq.tms.tms_microservice.modules.payrollManagement.model;

public class PayrollStatusUpdateModel {
    private String payrollId;
    private boolean isActive;

    public String getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(String payrollId) {
        this.payrollId = payrollId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

