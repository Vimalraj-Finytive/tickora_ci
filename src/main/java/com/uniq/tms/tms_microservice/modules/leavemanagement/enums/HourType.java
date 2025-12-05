package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum HourType {
    FIRST_Half("First Half"),
    SECOND_HALF("Second Half");

   private String value;

     HourType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
