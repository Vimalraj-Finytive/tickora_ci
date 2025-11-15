package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

public class PayRollSummaryDto {

    private String id;
    private String payrollName;

    public PayRollSummaryDto(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayrollName() {
        return payrollName;
    }

    public void setPayrollName(String payrollName) {
        this.payrollName = payrollName;
    }
}
