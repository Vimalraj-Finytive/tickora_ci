package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum ViewerType {
    APPROVER("Approver"),VIEWER("Viewer");

    private final String value;

    ViewerType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
