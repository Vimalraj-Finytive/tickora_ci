package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum Status {

    PENDING("Pending"),
    APPROVED("Approved"),
    CANCELLED("Cancelled"),
    REJECTED("Rejected");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    }
