package com.uniq.tms.tms_microservice.dto;

import java.time.LocalDate;

public class UserAttendanceDto {
    private Long userId;
    private LocalDate date;
    private Long statusId;

    public UserAttendanceDto(Long userId,LocalDate date, Long statusId) {
        this.userId = userId;
        this.date = date;
        this.statusId = statusId;
    }

    public UserAttendanceDto() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
