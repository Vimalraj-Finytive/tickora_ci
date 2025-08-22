package com.uniq.tms.tms_microservice.enums;

import java.util.Arrays;

public enum CountryEnum{

    INDIA("India","Asia/Kolkata");

    private String county;
    private String timeZone;

    CountryEnum(String county, String timeZone) {
        this.county = county;
        this.timeZone = timeZone;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public static String getTimeZoneByCountry(String county){
        return Arrays.stream(CountryEnum.values())
                .filter(c -> c.getCounty().equals(county))
                .map(CountryEnum::getTimeZone)
                .findAny()
                .orElse("UTC");
    }
}
