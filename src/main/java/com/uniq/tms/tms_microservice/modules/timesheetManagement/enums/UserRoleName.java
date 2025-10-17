package com.uniq.tms.tms_microservice.modules.timesheetManagement.enums;

public enum UserRoleName {
    SUPERADMIN("superAdmin"),ADMIN("admin"),MANAGER("manager"),STAFF("staff"),STUDENT("student");

    private final String roleName;


    UserRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
