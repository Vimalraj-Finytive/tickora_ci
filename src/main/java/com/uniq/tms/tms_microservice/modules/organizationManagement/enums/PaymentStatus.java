package com.uniq.tms.tms_microservice.modules.organizationManagement.enums;

public enum PaymentStatus {
    CREATED("Created"),
    AUTHORIZED("Authorized"),
    SUCCESS("Success"),
    PENDING("Pending"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    private final String displayValue;

    PaymentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
