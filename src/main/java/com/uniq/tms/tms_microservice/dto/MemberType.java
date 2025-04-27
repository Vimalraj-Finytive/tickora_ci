package com.uniq.tms.tms_microservice.dto;

public enum MemberType {
    SUPERVISOR("Supervisor"),
    MEMBER("Member");

    private final String value;

    MemberType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
