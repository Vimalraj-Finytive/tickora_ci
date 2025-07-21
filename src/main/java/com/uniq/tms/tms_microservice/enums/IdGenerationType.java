package com.uniq.tms.tms_microservice.enums;

public enum IdGenerationType {

    TICKORA("TK"),
    WORK_SCHEDULE("WS"),
    FIXED_WORK("WSFX"),
    FLEXIBLE_WORK("WSFL"),
    WEEKLY_WORK("WSWK"),
    WORK_SCHEDULE_TYPE("WSTY");

    private final String prefix ;

    IdGenerationType(String prefix){
        this.prefix = prefix;
    }

    public String getPrefix(){
        return prefix;
    }
}
