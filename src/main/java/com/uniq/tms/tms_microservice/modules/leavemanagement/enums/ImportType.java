package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum ImportType {

    AUTO("Auto"),
    MANUAL("Manual");

    private final String type;

    ImportType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

}
