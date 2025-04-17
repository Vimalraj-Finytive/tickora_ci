package com.uniq.tms.tms_microservice.model;

import java.time.LocalTime;

public class WorkSchedule {
    private Long scheduleId;
    private String scheduleName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String restDay;
    private boolean isDefault;
    private String type;
    private Long organizationId;

    public WorkSchedule() {
    }

    public WorkSchedule(Long scheduleId, String scheduleName, LocalTime startTime, String restDay, String type, boolean isDefault, Long organizationId, LocalTime endTime) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.startTime = startTime;
        this.restDay = restDay;
        this.type = type;
        this.isDefault = isDefault;
        this.organizationId = organizationId;
        this.endTime = endTime;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getRestDay() {
        return restDay;
    }

    public void setRestDay(String restDay) {
        this.restDay = restDay;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
