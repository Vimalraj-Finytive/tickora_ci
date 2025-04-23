package com.uniq.tms.tms_microservice.dto;

public enum Privilege {
    REGISTER_ORGANIZATION("Register Organization"),
    CREATE_NEW_MEMBER("Create new member"),
    LIST_OF_MEMBER("List of member"),
    EDIT_MEMBER_PROFILE("Edit member profile"),
    EDIT_MEMBER_ROLE("Edit Member Role"),
    DELETE_MEMBER("Delete member"),
    CREATE_NEW_GROUP("Create new group"),
    ADD_GROUP_MEMBER("Add group member"),
    CAN_SEE_OWN_TIMESHEET("Can see own timesheet"),
    CAN_SEE_ALL_TIMESHEETS("Can see all timesheets"),
    CAN_SEE_GROUP_LEVEL_TIMESHEETS("Can see group-level timesheets"),
    VIEW_SCHEDULE("View schedule"),
    REMOVE_GROUP_MEMBER("Remove group member"),
    EDIT_TIMESHEET("Edit Timesheet"),
    CREATE_NEW_LOCATION("Create new location"),
    STUDENT_ATTENDANCE("Student attendance"),
    VIEW_LOCATIONS("View locations"),
    DELETE_GROUP("Delete group"),
    CREATE_WORK_SCHEDULE("Create work schedule"),
    EDIT_GROUP("Edit group"),
    CHECK_IN_AND_OUT("Check in and check out");

    private final String dbValue;

    Privilege(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }
}
