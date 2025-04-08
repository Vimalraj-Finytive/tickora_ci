package com.uniq.tms.tms_microservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimesheetHistoryDto {

    private Long timesheetHistoryId;
    private Long locationId;
    private LocalTime logTime;
    private LogType logType;
    private LogFrom logFrom;
    private LocalDateTime loggedTimestamp;
    private Long userId;
    private LocalDate date;

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalTime logTime) {
        this.logTime = logTime;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public LogFrom getLogFrom() {
        return logFrom;
    }

    public void setLogFrom(LogFrom logFrom) {
        this.logFrom = logFrom;
    }
    public LocalDateTime getLoggedTimestamp() {
        return loggedTimestamp;
    }

    public void setLoggedTimestamp(LocalDateTime loggedTimestamp) {
        this.loggedTimestamp = loggedTimestamp;
    }

    public Long getTimesheetHistoryId() {
        return timesheetHistoryId;
    }

    public void setTimesheetHistoryId(Long timesheetHistoryId) {
        this.timesheetHistoryId = timesheetHistoryId;
    }
}
