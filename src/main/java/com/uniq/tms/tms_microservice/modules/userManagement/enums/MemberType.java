package com.uniq.tms.tms_microservice.modules.userManagement.enums;

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
