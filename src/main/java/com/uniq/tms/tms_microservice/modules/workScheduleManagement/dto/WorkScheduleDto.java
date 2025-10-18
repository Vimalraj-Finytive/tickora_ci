package com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class WorkScheduleDto {
    private String scheduleId;
    private String scheduleName;
    private boolean isActive;
    private boolean isDefault;
    private String type;
    private boolean isAutoClockOut;

    @Pattern(regexp = "^(?:[01]\\d|2[0-3]):00$", message = "Invalid time. Minutes not allowed")
    private String splitTime;

    @JsonIgnore
    private String orgId;
    private Double duration;
    private String typeName;
    private List<FixedScheduleDto> fixedSchedule;
    private List<FlexibleScheduleDto> flexibleSchedule;
    private WeeklyScheduleDto weeklySchedule;


    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public List<FixedScheduleDto> getFixedSchedule() {
        return fixedSchedule;
    }

    public void setFixedSchedule(List<FixedScheduleDto> fixedSchedule) {
        this.fixedSchedule = fixedSchedule;
    }

    public List<FlexibleScheduleDto> getFlexibleSchedule() {
        return flexibleSchedule;
    }

    public void setFlexibleSchedule(List<FlexibleScheduleDto> flexibleSchedule) {
        this.flexibleSchedule = flexibleSchedule;
    }

    public WeeklyScheduleDto getWeeklySchedule() {
        return weeklySchedule;
    }

    public void setWeeklySchedule(WeeklyScheduleDto weeklySchedule) {
        this.weeklySchedule = weeklySchedule;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSplitTime() { return splitTime; }

    public void setSplitTime(String splitTime) {
        this.splitTime = splitTime; }

    public boolean getAutoClockOut() {
        return isAutoClockOut;
    }

    public void setAutoClockOut(boolean autoClockOut) {
        isAutoClockOut = autoClockOut;
    }
}
