package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uniq.tms.tms_microservice.enums.LogFrom;
import com.uniq.tms.tms_microservice.enums.LogType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimesheetHistoryDto {

    private Long timesheetHistoryId;
    private String locationName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime logTime;
    private LogType logType;
    private LogFrom logFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime loggedTimestamp;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDate date;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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
