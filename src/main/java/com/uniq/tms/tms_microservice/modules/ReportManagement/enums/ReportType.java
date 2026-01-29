package com.uniq.tms.tms_microservice.modules.ReportManagement.enums;

public enum ReportType {

    TIMESHEET("Timesheet"),
    TIMEOFF_REQUEST("Time Off Request"),
    PAYROLL("PayRoll");

    private final String values;

    ReportType(String values){
        this.values = values;
    }

    public String getValues(){
        return values;
    }

    public static ReportType from(String values){
        return ReportType.valueOf(values.toUpperCase());
    }

}
