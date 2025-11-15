package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

public class PayrollStatusUpdateDto {
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

