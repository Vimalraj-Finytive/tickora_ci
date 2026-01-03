package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum ValidityStatus {
    EXPIRED("Expired"),
    ACTIVE("Active");
    private final String status;

    ValidityStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
