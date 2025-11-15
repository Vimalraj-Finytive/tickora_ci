package com.uniq.tms.tms_microservice.modules.payrollManagement.model;

public class PayRollSummary {

    private String id;
    private String payrollName;

    public PayRollSummary(String id, String payrollName){
        this.id = id;
        this.payrollName = payrollName;
    };
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
