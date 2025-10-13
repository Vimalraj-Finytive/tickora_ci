package com.uniq.tms.tms_microservice.modules.organizationManagement.enums;

public enum PaymentStatus {
    SUCCESS("Success"),
    FAILED("Failed");

    private final String displayValue;

    PaymentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
