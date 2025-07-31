package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uniq.tms.tms_microservice.constant.UserConstant;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    public UserTimesheetDto(String userId, String userName, LocalDate date, String workStatus, LocalTime firstClockIn, LocalTime lastClockOut, Duration trackedHours, Duration regularHours, String firstClockInTime, String lastClockOutTime, String trackedHoursDuration, String regularHoursDuration, List<TimesheetHistoryDto> history, String status, String workScheduleName) {
        this.userId = userId;
        this.userName = userName;
        this.date = date;
        this.workStatus = workStatus;
        this.firstClockIn = firstClockIn;
        this.lastClockOut = lastClockOut;
        this.trackedHours = trackedHours;
        this.regularHours = regularHours;
        this.status = status;
        this.workScheduleName = workScheduleName;
        this.firstClockInTime = formatTime(this.firstClockIn);
        this.lastClockOutTime = formatTime(this.lastClockOut);
        this.trackedHoursDuration = formatDuration(this.trackedHours);
        this.regularHoursDuration = formatDuration(this.regularHours);
        this.history = history;
    }

    public UserTimesheetDto() {
    }

    private Duration convertToDuration(LocalTime localTime) {
        if (localTime == null) {
            return Duration.ZERO;
        }
        return Duration.ofHours(localTime.getHour())
                .plusMinutes(localTime.getMinute())
                .plusSeconds(localTime.getSecond());
    }

    // Method to format LocalTime to 12-hour format (AM/PM)
    private String formatTime(LocalTime localTime) {
        if (localTime == null) {
            return "00:00";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(UserConstant.TWELVE_HOUR_FORMAT); // 12-hour format with AM/PM
        return localTime.format(formatter);
    }

    // Method to convert Duration to "XXh XXm" format
    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "00h 00m";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%02dh %02dm", hours, minutes); // Format as "09h 00m"
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public String getFirstClockInTime() {
        return firstClockInTime;
    }

    public void setFirstClockInTime(String firstClockInTime) {
        this.firstClockInTime = firstClockInTime;
    }

    public String getLastClockOutTime() {
        return lastClockOutTime;
    }

    public void setLastClockOutTime(String lastClockOutTime) {
        this.lastClockOutTime = lastClockOutTime;
    }

    public String getTrackedHoursDuration() {
        return trackedHoursDuration;
    }

    public void setTrackedHoursDuration(String trackedHoursDuration) {
        this.trackedHoursDuration = trackedHoursDuration;
    }

    public String getRegularHoursDuration() {
        return regularHoursDuration;
    }

    public void setRegularHoursDuration(String regularHoursDuration) {
        this.regularHoursDuration = regularHoursDuration;
    }

    public List<TimesheetHistoryDto> getHistory() {
        return history;
    }

    public void setHistory(List<TimesheetHistoryDto> history) {
        this.history = history;
    }

    public LocalTime getFirstClockIn() {
        return firstClockIn;
    }

    public void setFirstClockIn(LocalTime firstClockIn) {
        this.firstClockIn = firstClockIn;
    }

    public LocalTime getLastClockOut() {
        return lastClockOut;
    }

    public void setLastClockOut(LocalTime lastClockOut) {
        this.lastClockOut = lastClockOut;
    }

    public Duration getTrackedHours() {
        return trackedHours;
    }

    public void setTrackedHours(Duration trackedHours) {
        this.trackedHours = trackedHours;
    }

    public Duration getRegularHours() {
        return regularHours;
    }

    public void setRegularHours(Duration regularHours) {
        this.regularHours = regularHours;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkScheduleName() {
        return workScheduleName;
    }

    public void setWorkScheduleName(String workScheduleName) {
        this.workScheduleName = workScheduleName;
    }
}
