package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimesheetDto {
    private Long id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalTime firstClockIn;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalTime lastClockOut;
    @JsonIgnore
    private Duration trackedHours;
    @JsonIgnore
    private Duration regularHours;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime createdAt;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime updatedAt;
    private String dayType;
    private String workStatus;
    private String userDayType;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String role;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String firstClockInTime;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String lastClockOutTime;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String trackedHoursDuration;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String regularHoursDuration;
    private List<TimesheetHistoryDto> history;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String statusId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String groupname;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean paidLeave;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String mobileNumber;
    private String status;
    private String workScheduleName;

    public TimesheetDto(TimesheetEntity timesheetEntity, List<TimesheetHistoryDto> historyDtos, String workScheduleName) {
        this.id = timesheetEntity.getId();
        this.userId = timesheetEntity.getUserId();
        this.date = timesheetEntity.getDate();
        this.firstClockIn = timesheetEntity.getFirstClockIn();
        this.lastClockOut = timesheetEntity.getLastClockOut();
        this.regularHours = convertToDuration(timesheetEntity.getRegularHours());
        this.trackedHours = convertToDuration(timesheetEntity.getTrackedHours());
        this.workScheduleName = workScheduleName;
        this.firstClockInTime = formatTime(this.firstClockIn);
        this.lastClockOutTime = formatTime(this.lastClockOut);
        this.trackedHoursDuration = formatDuration(this.trackedHours);
        this.regularHoursDuration = formatDuration(this.regularHours);
        this.history = historyDtos;
    }

    public TimesheetDto() {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDayType() {
        return userDayType;
    }

    public void setUserDayType(String userDayType) {
        this.userDayType = userDayType;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TimesheetHistoryDto> getHistory() {
        return history;
    }

    public void setHistory(List<TimesheetHistoryDto> history) {
        this.history = history;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public Boolean getPaidLeave() {
        return paidLeave;
    }

    public void setPaidLeave(Boolean paidLeave) {
        this.paidLeave = paidLeave;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
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
