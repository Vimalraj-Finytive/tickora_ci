package com.uniq.tms.tms_microservice.dto;

import java.time.LocalTime;

public class WorkScheduleDto {
    private String scheduleName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String restDay;
    private boolean isDefault;
    private String type;
    private Long organizationId;

    public WorkScheduleDto() {
    }

    public WorkScheduleDto(String scheduleName, LocalTime startTime, String restDay, boolean isDefault, Long organizationId, String type, LocalTime endTime) {
        this.scheduleName = scheduleName;
        this.startTime = startTime;
        this.restDay = restDay;
        this.isDefault = isDefault;
        this.organizationId = organizationId;
        this.type = type;
        this.endTime = endTime;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
