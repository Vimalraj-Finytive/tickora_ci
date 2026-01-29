package com.uniq.tms.tms_microservice.modules.ReportManagement.enums;

public enum ReportStatus {

    PROCESSING("Processing"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed");

    private final String values;

    ReportStatus(String values){
        this.values = values;
    }

    public String getValues(){
        return values;
    }
}
