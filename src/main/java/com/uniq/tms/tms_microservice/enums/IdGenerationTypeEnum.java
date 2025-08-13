package com.uniq.tms.tms_microservice.enums;

public enum IdGenerationTypeEnum {

    TICKORA("TK"),
    USER("UI"),
    WORK_SCHEDULE("WS"),
    FIXED_WORK("WSFX"),
    FLEXIBLE_WORK("WSFL"),
    WEEKLY_WORK("WSWK"),
    WORK_SCHEDULE_TYPE("WSTY"),
    SECONDARY_USER("USI");

    private final String prefix ;

    IdGenerationTypeEnum(String prefix){
        this.prefix = prefix;
    }

    public String getPrefix(){
        return prefix;
    }
}
