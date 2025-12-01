package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum ReportType {

    PROCESSING("Processing"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed");

    private final String values;

    ReportType(String values){
        this.values = values;
    }

    public String getValues(){
        return values;
    }
}
