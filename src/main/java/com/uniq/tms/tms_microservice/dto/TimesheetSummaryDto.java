package com.uniq.tms.tms_microservice.dto;

public class TimesheetSummaryDto {
    private Long userId;
    private String userName;
    private String mobileNumber;
    private String role;
    private String groupname;
    private int presentCount;
    private int absentCount;
    private int notMarkedCount;
    private int paidLeaveCount;
    private int holidayCount;
    private Integer totalCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
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
}
