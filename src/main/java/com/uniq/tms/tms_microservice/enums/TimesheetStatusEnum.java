package com.uniq.tms.tms_microservice.enums;

public enum TimesheetStatusEnum {

    PRESENT("TSS001","Present"),
    ABSENT("TSS002","Absent"),
    PAID_LEAVE("TSS003","Paid Leave"),
    NOT_MARKED( "TSS004","Not Marked"),
    HOLIDAY("TSS005","Holiday"),
    HALF_DAY( "TSS006","Half Day"),
    PERMISSION("TSS007","Permission");

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
