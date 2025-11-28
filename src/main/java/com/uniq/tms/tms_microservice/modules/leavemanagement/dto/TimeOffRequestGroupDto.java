package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ViewerType;
import java.time.LocalDate;

public class TimeOffRequestGroupDto {
    private String userId;
    private String policyName;
    private String userName;
    private LocalDate requestDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startTime;
    private String endTime;
    private Integer unitsRequested;
    private Double hoursRequested;
    private String reason;
    private String status;
    private ViewerType viewerType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getUnitsRequested() {
        return unitsRequested;
    }

    public void setUnitsRequested(Integer unitsRequested) {
        this.unitsRequested = unitsRequested;
    }

    public Double getHoursRequested() {
        return hoursRequested;
    }

    public void setHoursRequested(Double hoursRequested) {
        this.hoursRequested = hoursRequested;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ViewerType getViewerType() {
        return viewerType;
    }

    public void setViewerType(ViewerType viewerType) {
        this.viewerType = viewerType;
    }
}
