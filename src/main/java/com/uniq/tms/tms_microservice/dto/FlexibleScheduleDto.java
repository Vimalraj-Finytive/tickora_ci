package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonGetter;

public class FlexibleScheduleDto {

    private String day;
    private Double duration;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    @JsonGetter("duration")
    public String getFormattedDuration() {
        if (duration == null) return null;
        int hours = duration.intValue();
        int minutes = (int) Math.round((duration - hours) * 60);
        return String.format("%02dh %02dm", hours, minutes);
    }
}
