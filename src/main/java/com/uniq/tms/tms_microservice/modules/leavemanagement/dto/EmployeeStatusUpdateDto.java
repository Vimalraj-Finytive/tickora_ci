package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;

import java.time.LocalDate;

public class EmployeeStatusUpdateDto {
    private String userId;
    private Status status;
    private String reason;
    private Integer unitsRequested;
    private Integer hoursRequested;
    private String startAndEndDates;
    private String startAndEndTimes;
    private LocalDate requestDate;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getUnitsRequested() {
        return unitsRequested;
    }

    public void setUnitsRequested(Integer unitsRequested) {
        this.unitsRequested = unitsRequested;
    }

    public Integer getHoursRequested() {
        return hoursRequested;
    }

    public void setHoursRequested(Integer hoursRequested) {
        this.hoursRequested = hoursRequested;
    }

    public String getStartAndEndDates() {
        return startAndEndDates;
    }

    public void setStartAndEndDates(String startAndEndDates) {
        this.startAndEndDates = startAndEndDates;
    }

    public String getStartAndEndTimes() {
        return startAndEndTimes;
    }

    public void setStartAndEndTimes(String startAndEndTimes) {
        this.startAndEndTimes = startAndEndTimes;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }
}
