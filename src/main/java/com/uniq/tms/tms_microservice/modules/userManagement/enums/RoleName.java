package com.uniq.tms.tms_microservice.modules.userManagement.enums;

public enum RoleName {
    SUPERADMIN("SuperAdmin"),
    ADMIN("Admin"),
    MANAGER("Manager"),
    STAFF("Staff"),
    STUDENT("Student");

    private String roleName;
    RoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName(){
        return roleName;
    }
}
