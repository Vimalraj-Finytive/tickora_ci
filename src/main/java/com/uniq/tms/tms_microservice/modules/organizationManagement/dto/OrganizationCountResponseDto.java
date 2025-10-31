package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

public class OrganizationCountResponseDto {

    private long currentMonthCount;
    private long previousMonthCount;

    public OrganizationCountResponseDto(long currentMonthCount, long previousMonthCount) {
        this.currentMonthCount = currentMonthCount;
        this.previousMonthCount = previousMonthCount;
    }

    public long getCurrentMonthCount() {
        return currentMonthCount;
    }

    public void setCurrentMonthCount(long currentMonthCount) {
        this.currentMonthCount = currentMonthCount;
    }

    public long getPreviousMonthCount() {
        return previousMonthCount;
    }

    public void setPreviousMonthCount(long previousMonthCount) {
        this.previousMonthCount = previousMonthCount;
    }
}

