package com.uniq.tms.tms_microservice.modules.timesheetManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogFrom;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimesheetHistoryDto {

    private Long timesheetHistoryId;
    private String locationName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long locationId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private String logTime;
    private LogType logType;
    private LogFrom logFrom;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDate date;
    private LocalDateTime loggedTimestamp;

    public TimesheetHistoryDto(Long timesheetHistoryId, String locationName, String logTime, LogType logType,
                               LogFrom logFrom) {
        this.timesheetHistoryId = timesheetHistoryId;
        this.locationName = locationName;
        this.logTime = logTime;
        this.logType = logType;
        this.logFrom = logFrom;
    }

    public TimesheetHistoryDto() {

    }

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

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
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

    public Long getTimesheetHistoryId() {
        return timesheetHistoryId;
    }

    public void setTimesheetHistoryId(Long timesheetHistoryId) {
        this.timesheetHistoryId = timesheetHistoryId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public LocalDateTime getLoggedTimestamp() {
        return loggedTimestamp;
    }

    public void setLoggedTimestamp(LocalDateTime loggedTimestamp) {
        this.loggedTimestamp = loggedTimestamp;
    }
}
