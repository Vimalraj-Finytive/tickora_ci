package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import java.time.LocalDate;

public class TimeOffccDto {
    private String userId;
    private LocalDate fromDate;
    private LocalDate toDate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}
