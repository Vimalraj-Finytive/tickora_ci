package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum Compensation {
    PAID("Paid"),
    UNPAID("UnPaid");
    private final String value;

    Compensation(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
