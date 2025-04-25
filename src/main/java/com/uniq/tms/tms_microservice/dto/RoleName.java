package com.uniq.tms.tms_microservice.dto;

public enum RoleName {
    SUPERADMIN,
    ADMIN,
    MANAGER,
    STAFF,
    STUDENT;

    public String getRoleName(){
        return name();
    }
}
