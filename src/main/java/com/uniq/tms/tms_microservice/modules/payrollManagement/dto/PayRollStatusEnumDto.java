package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

public class PayRollStatusEnumDto {

    private String key;
    private String value;

    public PayRollStatusEnumDto() {}

    public PayRollStatusEnumDto(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }

    public void setKey(String key) { this.key = key; }
    public void setValue(String value) { this.value = value; }
}
