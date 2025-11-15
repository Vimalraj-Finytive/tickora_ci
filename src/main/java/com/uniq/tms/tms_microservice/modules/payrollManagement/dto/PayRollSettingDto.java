package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

public class PayRollSettingDto {

    private String payrollCalculation;
    private boolean isOvertime;

    public PayRollSettingDto() {
    }


    public String getPayrollCalculation() {
        return payrollCalculation;
    }

    public void setPayrollCalculation(String payrollCalculation) {
        this.payrollCalculation = payrollCalculation;
    }

    public boolean isOvertime() {
        return isOvertime;
    }

    public void setOvertime(boolean overtime) {
        isOvertime = overtime;
    }

}
