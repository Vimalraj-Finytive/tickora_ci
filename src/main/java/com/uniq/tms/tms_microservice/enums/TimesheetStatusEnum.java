package com.uniq.tms.tms_microservice.enums;

public enum TimesheetStatusEnum {

    PRESENT("Present"),
    ABSENT("Absent"),
    PAID_LEAVE("Paid Leave"),
    NOT_MARKED( "Not Marked"),
    HOLIDAY("Holiday"),
    HALF_DAY( "Half Day"),
    PERMISSION("Permission");

    private final String label;

    TimesheetStatusEnum( String label) {  this.label = label; }

    public String getLabel() {
        return label;
    }

}
