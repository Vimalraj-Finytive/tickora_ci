package com.uniq.tms.tms_microservice.modules.timesheetManagement.enums;

public enum TimesheetWorkStatusEnum {

    OVERTIME("OverTime"),
    SUFFICIENT_HOURS("Sufficient Hours"),
    LESS_WORKED_HOURS("Less Worked Hours"),
    EXTRA_WORKED_DAY("Extra Worked Day"),
    FAILED_CLOCK_OUT("Failed clock out"),
    TIME_OFF("Time Off");

    private final String label;

    TimesheetWorkStatusEnum(String labelName){
        this.label = labelName;
    }

    public String getLabel(){
        return label;
    }
}
