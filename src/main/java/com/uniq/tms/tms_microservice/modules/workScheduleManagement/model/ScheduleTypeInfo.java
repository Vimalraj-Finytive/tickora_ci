package com.uniq.tms.tms_microservice.modules.workScheduleManagement.model;

import java.time.Duration;
import java.time.LocalTime;

public class ScheduleTypeInfo {

    private final boolean fixed;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Duration duration;

    private ScheduleTypeInfo(boolean fixed, LocalTime startTime, LocalTime endTime, Duration duration) {
        this.fixed = fixed;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public static ScheduleTypeInfo fixed(LocalTime startTime, LocalTime endTime) {
        return new ScheduleTypeInfo(true, startTime, endTime, Duration.between(startTime, endTime));
    }

    public static ScheduleTypeInfo flexible(Duration duration) {
        return new ScheduleTypeInfo(false, null, null, duration);
    }

    public boolean isFixed() { return fixed; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Duration getDuration() { return duration; }

}
