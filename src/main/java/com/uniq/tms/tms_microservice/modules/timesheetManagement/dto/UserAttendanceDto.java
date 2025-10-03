package com.uniq.tms.tms_microservice.modules.timesheetManagement.dto;

import java.time.LocalDate;

public class UserAttendanceDto {
    private String userId;
    private LocalDate date;
    private String status;

    public UserAttendanceDto(String userId,LocalDate date, String status) {
        this.userId = userId;
        this.date = date;
        this.status = status;
    }

    public UserAttendanceDto() {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
