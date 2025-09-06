package com.uniq.tms.tms_microservice.model;

import com.uniq.tms.tms_microservice.enums.LogFrom;
import com.uniq.tms.tms_microservice.enums.LogType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimesheetHistory {

    private String locationName;
    private LocalTime logTime;
    private LogType logType;
    private LogFrom logFrom;
    private LocalDateTime loggedTimestamp;
    private String userId;
    private LocalDate date;
    private Long locationId;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
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

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}
