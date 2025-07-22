package com.uniq.tms.tms_microservice.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Timesheet {

    private Long id;
    private String userId;
    private LocalDate date;
    private LocalTime firstClockIn;
    private LocalTime lastClockOut;
    private Duration trackedHours;
    private Duration regularHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;
    private String firstClockInStr;
    private String lastClockOutStr;
    private String trackedHoursStr;
    private String regularHoursStr;
    private String statusId;
    private String mobileNumber;
    private String Status;
    private String workScheduleName;
    private int presentCount;
    private int absentCount;
    private int notMarkedCount;
    private int paidLeaveCount;
    private int holidayCount;

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

    public String getFirstClockInStr() {
        return firstClockInStr;
    }

    public void setFirstClockInStr(String firstClockInStr) {
        this.firstClockInStr = firstClockInStr;
    }

    public String getLastClockOutStr() {
        return lastClockOutStr;
    }

    public void setLastClockOutStr(String lastClockOutStr) {
        this.lastClockOutStr = lastClockOutStr;
    }

    public String getTrackedHoursStr() {
        return trackedHoursStr;
    }

    public void setTrackedHoursStr(String trackedHoursStr) {
        this.trackedHoursStr = trackedHoursStr;
    }

    public String getRegularHoursStr() {
        return regularHoursStr;
    }

    public void setRegularHoursStr(String regularHoursStr) {
        this.regularHoursStr = regularHoursStr;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public int getNotMarkedCount() {
        return notMarkedCount;
    }

    public void setNotMarkedCount(int notMarkedCount) {
        this.notMarkedCount = notMarkedCount;
    }

    public int getPaidLeaveCount() {
        return paidLeaveCount;
    }

    public void setPaidLeaveCount(int paidLeaveCount) {
        this.paidLeaveCount = paidLeaveCount;
    }

    public int getHolidayCount() {
        return holidayCount;
    }

    public void setHolidayCount(int holidayCount) {
        this.holidayCount = holidayCount;
    }

    public String getWorkScheduleName() {
        return workScheduleName;
    }

    public void setWorkScheduleName(String workScheduleName) {
        this.workScheduleName = workScheduleName;
    }
}
