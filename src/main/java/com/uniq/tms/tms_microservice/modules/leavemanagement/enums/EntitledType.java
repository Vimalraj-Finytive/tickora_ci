package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum EntitledType {

    DAY("Day"),
    HOURS("Hours"),
    HALF_DAY("Half Day");

    private final String value;

    EntitledType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}