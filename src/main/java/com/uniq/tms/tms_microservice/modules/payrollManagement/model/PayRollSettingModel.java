package com.uniq.tms.tms_microservice.modules.payrollManagement.model;

import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollSettingEnum;

public class PayRollSettingModel {

    private PayRollSettingEnum payrollCalculation;
    private boolean isOvertime;


    public PayRollSettingEnum getPayrollCalculation() {
        return payrollCalculation;
    }

    public void setPayrollCalculation(PayRollSettingEnum payrollCalculation) {
        this.payrollCalculation = payrollCalculation;
    }

    public boolean isOvertime() {
        return isOvertime;
    }

    public void setOvertime(boolean overtime) {
        isOvertime = overtime;
    }

}
