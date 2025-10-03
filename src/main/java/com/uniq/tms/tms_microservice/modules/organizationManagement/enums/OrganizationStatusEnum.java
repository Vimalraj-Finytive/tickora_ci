package com.uniq.tms.tms_microservice.modules.organizationManagement.enums;


public enum OrganizationStatusEnum {

    ACTIVE("Active"),
    EXPIRED("Expired"),
    SUSPENDED("Suspended");

    private final String displayValue;

    OrganizationStatusEnum(String displayValue){
        this.displayValue = displayValue;
    }

    public String getDisplayValue(){
        return displayValue;
    }
}
