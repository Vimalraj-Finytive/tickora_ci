package com.uniq.tms.tms_microservice.enums;

public enum CountryEnum{
    INDIA("India");

    private String county;

    CountryEnum(String county) {
        this.county = county;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }
}
