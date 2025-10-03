package com.uniq.tms.tms_microservice.modules.timesheetManagement.dto;

public class TimesheetSummaryDto {
    private String userId;
    private String userName;
    private String mobileNumber;
    private String role;
    private String groupName;
    private int presentCount;
    private int absentCount;
    private int notMarkedCount;
    private int paidLeaveCount;
    private int holidayCount;
    private int halfDayCount;
    private int permissionCount;
    private Integer totalCount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public int getHalfDayCount() {
        return halfDayCount;
    }

    public void setHalfDayCount(int halfDayCount) {
        this.halfDayCount = halfDayCount;
    }

    public int getPermissionCount() {
        return permissionCount;
    }

    public void setPermissionCount(int permissionCount) {
        this.permissionCount = permissionCount;
    }
}
