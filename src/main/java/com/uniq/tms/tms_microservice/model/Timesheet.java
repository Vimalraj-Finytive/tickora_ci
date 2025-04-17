package com.uniq.tms.tms_microservice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Timesheet {

    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalTime firstClockIn;
    private LocalTime lastClockOut;
    private LocalTime trackedHours;
    private LocalTime regularHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalTime getRegularHours() {
        return regularHours;
    }

    public void setRegularHours(LocalTime regularHours) {
        this.regularHours = regularHours;
    }

    public LocalTime getTrackedHours() {
        return trackedHours;
    }

    public void setTrackedHours(LocalTime trackedHours) {
        this.trackedHours = trackedHours;
    }

    public LocalTime getLastClockOut() {
        return lastClockOut;
    }

    public void setLastClockOut(LocalTime lastClockOut) {
        this.lastClockOut = lastClockOut;
    }

    public LocalTime getFirstClockIn() {
        return firstClockIn;
    }

    public void setFirstClockIn(LocalTime firstClockIn) {
        this.firstClockIn = firstClockIn;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
