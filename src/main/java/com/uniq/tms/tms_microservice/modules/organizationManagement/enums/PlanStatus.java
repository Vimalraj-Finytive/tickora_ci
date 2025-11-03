package com.uniq.tms.tms_microservice.modules.organizationManagement.enums;

public enum PlanStatus {

    Inactive("Inactive"),
    No_ACTIVE_PLAN("No Active Plan"),
    EXPIRED("Expired");

    public String planStatus;

    public String getPlanStatus(){
        return planStatus;
    }

    PlanStatus(String status){
        this.planStatus = status;
    }
}
