package com.uniq.tms.tms_microservice.modules.timesheetManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class UserTimesheetDto {

    @JsonIgnore
    private String userId;
    private String userName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String workStatus;
    @JsonIgnore
    private LocalTime firstClockIn;
    @JsonIgnore
    private LocalTime lastClockOut;
    @JsonIgnore
    private Duration trackedHours;
    @JsonIgnore
    private Duration regularHours;
    private String firstClockInTime;
    private String lastClockOutTime;
    private String trackedHoursDuration;
    private String regularHoursDuration;
    private List<TimesheetHistoryDto> history;
    private String status;
    private String workScheduleName;

    public UserTimesheetDto(String userName, TimesheetDto t) {
        this.userName = userName;
        this.date = t.getDate();
        this.workStatus = t.getWorkStatus();
        this.firstClockInTime = t.getFirstClockInTime();
        this.lastClockOutTime = t.getLastClockOutTime();
        this.trackedHoursDuration = t.getTrackedHoursDuration();
        this.regularHoursDuration = t.getRegularHoursDuration();
        this.history = t.getHistory();
        this.status = t.getStatus();
        this.workScheduleName = t.getWorkScheduleName();
    }

    public UserTimesheetDto() {

    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getWorkStatus() { return workStatus; }
    public void setWorkStatus(String workStatus) { this.workStatus = workStatus; }

    public LocalTime getFirstClockIn() { return firstClockIn; }
    public void setFirstClockIn(LocalTime firstClockIn) { this.firstClockIn = firstClockIn; }

    public LocalTime getLastClockOut() { return lastClockOut; }
    public void setLastClockOut(LocalTime lastClockOut) { this.lastClockOut = lastClockOut; }

    public Duration getTrackedHours() { return trackedHours; }
    public void setTrackedHours(Duration trackedHours) { this.trackedHours = trackedHours; }

    public Duration getRegularHours() { return regularHours; }
    public void setRegularHours(Duration regularHours) { this.regularHours = regularHours; }

    public String getFirstClockInTime() { return firstClockInTime; }
    public void setFirstClockInTime(String firstClockInTime) { this.firstClockInTime = firstClockInTime; }

    public String getLastClockOutTime() { return lastClockOutTime; }
    public void setLastClockOutTime(String lastClockOutTime) { this.lastClockOutTime = lastClockOutTime; }

    public String getTrackedHoursDuration() { return trackedHoursDuration; }
    public void setTrackedHoursDuration(String trackedHoursDuration) { this.trackedHoursDuration = trackedHoursDuration; }

    public String getRegularHoursDuration() { return regularHoursDuration; }
    public void setRegularHoursDuration(String regularHoursDuration) { this.regularHoursDuration = regularHoursDuration; }

    public List<TimesheetHistoryDto> getHistory() { return history; }
    public void setHistory(List<TimesheetHistoryDto> history) { this.history = history; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWorkScheduleName() { return workScheduleName; }
    public void setWorkScheduleName(String workScheduleName) { this.workScheduleName = workScheduleName; }
}
