package com.uniq.tms.tms_microservice.enums;

public enum UserStatusTypeEnum {

    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String value;

    UserStatusTypeEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
