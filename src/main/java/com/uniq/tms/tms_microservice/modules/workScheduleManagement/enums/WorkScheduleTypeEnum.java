package com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums;

public enum WorkScheduleTypeEnum {

    FIXED("Fixed"),
    FLEXIBLE("Flexible"),
    WEEKLY("Weekly");

    private final String scheduleType;

    WorkScheduleTypeEnum(String type){
        this.scheduleType = type;
    }
    public String getScheduleType(){
        return scheduleType;
    }
}
