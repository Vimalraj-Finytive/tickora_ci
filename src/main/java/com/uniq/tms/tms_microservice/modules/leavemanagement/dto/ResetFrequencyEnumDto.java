package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

public class ResetFrequencyEnumDto {
    private String name;
    private String value;

    public ResetFrequencyEnumDto(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

