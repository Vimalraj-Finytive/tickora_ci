package com.uniq.tms.tms_microservice.modules.payrollManagement.enums;

public enum PayRollStatusEnum {

    PROCESSING("Processing"),
    APPROVED("Approved"),
    PAID("Paid"),
    CANCELLED("Cancelled"),
    FAILED("Failed"),
    NOTGENERATED("not_generated");


    private final String value;

    PayRollStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
