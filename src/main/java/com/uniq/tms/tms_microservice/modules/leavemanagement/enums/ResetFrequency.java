package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum ResetFrequency {

    MONTHLY("Monthly"),
    ANNUALLY("Annually");

    private final String value;

    ResetFrequency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
