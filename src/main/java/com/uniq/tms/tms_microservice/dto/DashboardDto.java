package com.uniq.tms.tms_microservice.dto;

import java.time.LocalDate;

public class DashboardDto {

    private LocalDate fromDate;
    private LocalDate toDate;
    private Long userId;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
