package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.HourType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimeOffRequestDto {

    @NotNull(message = "Policy ID cannot be null")
    private String policyId;
    private String userId;
    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;
    @NotNull(message = "end date cannot be null")
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    @NotNull(message = "unitsRequested cannot be null")
    private Integer unitsRequested;
    private String reason;
    private HourType hourType;

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getUnitsRequested() {
        return unitsRequested;
    }

    public void setUnitsRequested(Integer unitsRequested) {
        this.unitsRequested = unitsRequested;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public HourType getHourType() {
        return hourType;
    }

    public void setHourType(HourType hourType) {
        this.hourType = hourType;
    }
}
