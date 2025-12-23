package com.uniq.tms.tms_microservice.modules.timesheetManagement.enums;

public enum TimesheetStatusEnum {

    PRESENT("TSS001","Present"),
    ABSENT("TSS002","Absent"),
    PAID_LEAVE("TSS003","Paid Leave"),
    NOT_MARKED( "TSS004","Not Marked"),
    PUBLIC_HOLIDAY("TSS005","Public Holiday"),
    HALF_DAY( "TSS006","Half Day"),
    PERMISSION("TSS007","Permission"),
    REST_DAY("TSS008","Rest Day"),
    UNPAID_LEAVE("TSS009","Unpaid Leave");

    private final String id;
    private final String label;

    TimesheetStatusEnum( String id ,String label) {
        this.id = id;
        this.label = label;
    }

    public String getId(){
        return id;
    }
    public String getLabel() {
        return label;
    }
}
