package com.uniq.tms.tms_microservice.dto;

public class UserDashboardDto {

    private Integer presentCount;
    private Integer absentCount;
    private Integer paidLeaveCount;
    private Integer notMarkedCount;
    private Integer holidayCount;
    private Integer halfDayCount;
    private Integer permissionCount;
    private Integer totalCount;
    private Double presentPercentage;
    private Double absentPercentage;
    private Double paidLeavePercentage;
    private Double notMarkedPercentage;
    private Double holidayPercentage;
    private Double halfDayPercentage;
    private Double permissionPercentage;

    public Integer getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(Integer presentCount) {
        this.presentCount = presentCount;
    }

    public Integer getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(Integer absentCount) {
        this.absentCount = absentCount;
    }

    public Integer getPaidLeaveCount() {
        return paidLeaveCount;
    }

    public void setPaidLeaveCount(Integer paidLeaveCount) {
        this.paidLeaveCount = paidLeaveCount;
    }

    public Integer getNotMarkedCount() {
        return notMarkedCount;
    }

    public void setNotMarkedCount(Integer notMarkedCount) {
        this.notMarkedCount = notMarkedCount;
    }

    public Integer getHolidayCount() {
        return holidayCount;
    }

    public void setHolidayCount(Integer holidayCount) {
        this.holidayCount = holidayCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Double getPresentPercentage() {
        return presentPercentage;
    }

    public void setPresentPercentage(Double presentPercentage) {
        this.presentPercentage = presentPercentage;
    }

    public Double getAbsentPercentage() {
        return absentPercentage;
    }

    public void setAbsentPercentage(Double absentPercentage) {
        this.absentPercentage = absentPercentage;
    }

    public Double getPaidLeavePercentage() {
        return paidLeavePercentage;
    }

    public void setPaidLeavePercentage(Double paidLeavePercentage) {
        this.paidLeavePercentage = paidLeavePercentage;
    }

    public Double getNotMarkedPercentage() {
        return notMarkedPercentage;
    }

    public void setNotMarkedPercentage(Double notMarkedPercentage) {
        this.notMarkedPercentage = notMarkedPercentage;
    }

    public Double getHolidayPercentage() {
        return holidayPercentage;
    }

    public void setHolidayPercentage(Double holidayPercentage) {
        this.holidayPercentage = holidayPercentage;
    }

    public Integer getHalfDayCount() {
        return halfDayCount;
    }

    public void setHalfDayCount(Integer halfDayCount) {
        this.halfDayCount = halfDayCount;
    }

    public Integer getPermissionCount() {
        return permissionCount;
    }

    public void setPermissionCount(Integer permissionCount) {
        this.permissionCount = permissionCount;
    }

    public Double getHalfDayPercentage() {
        return halfDayPercentage;
    }

    public void setHalfDayPercentage(Double halfDayPercentage) {
        this.halfDayPercentage = halfDayPercentage;
    }

    public Double getPermissionPercentage() {
        return permissionPercentage;
    }

    public void setPermissionPercentage(Double permissionPercentage) {
        this.permissionPercentage = permissionPercentage;
    }
}
