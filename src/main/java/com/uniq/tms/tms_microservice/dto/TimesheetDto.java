package com.uniq.tms.tms_microservice.dto;


import com.uniq.tms.tms_microservice.entity.TimesheetEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TimesheetDto {
    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalTime firstClockIn;
    private LocalTime lastClockOut;
    private LocalTime trackedHours;
    private LocalTime regularHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TimesheetHistoryDto> history;
    private String dayType;
    private String workStatus;
    private String userDayType;
    private String userName;
    private String role;


    public TimesheetDto(TimesheetEntity timesheetEntity, List<TimesheetHistoryDto> historyDtos) {
        this.id = timesheetEntity.getId();
        this.userId = timesheetEntity.getUserId();
        this.date = timesheetEntity.getDate();
        this.firstClockIn = timesheetEntity.getFirstClockIn();
        this.lastClockOut = timesheetEntity.getLastClockOut();
        this.regularHours = timesheetEntity.getRegularHours();
        this.trackedHours = timesheetEntity.getTrackedHours();
        this.history = historyDtos;
    }

    public TimesheetDto() {
    }

    public TimesheetDto(Object[] obj) {
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

    public LocalTime getTrackedHours() {
        return trackedHours;
    }

    public void setTrackedHours(LocalTime trackedHours) {
        this.trackedHours = trackedHours;
    }

    public LocalTime getRegularHours() {
        return regularHours;
    }

    public void setRegularHours(LocalTime regularHours) {
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
}
