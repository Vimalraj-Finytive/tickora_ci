package com.uniq.tms.tms_microservice.modules.payrollManagement.enums;

public enum PayRollSettingEnum {
    HOURS("Hours"),
    DAYS("Days");

    private final String value;

    PayRollSettingEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}