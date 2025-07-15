package com.uniq.tms.tms_microservice.dto;

public class FlexibleScheduleDto {

    private String day;
    private double duration;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
