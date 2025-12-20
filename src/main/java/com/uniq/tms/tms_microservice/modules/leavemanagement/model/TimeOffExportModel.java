package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import java.time.LocalDate;
import java.util.List;

public class TimeOffExportModel {

    private String userId;
    private String userName;
    private String policyName;
    private String policyId;
    private LocalDate requestDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startTime;
    private String endTime;
    private Double unitsRequested;
    private String reason;
    private String status;
    private String leaveType;
    private String hourType;
    private List<ViewerModel> viewers;
    private List<ViewerModel> approver;
    private Long timeoffRequestId;

    public Long getTimeoffRequestId() {
        return timeoffRequestId;
    }

    public void setTimeoffRequestId(Long timeoffRequestId) {
        this.timeoffRequestId = timeoffRequestId;
    }

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

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
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

    public Double getUnitsRequested() {
        return unitsRequested;
    }

    public void setUnitsRequested(Double unitsRequested) {
        this.unitsRequested = unitsRequested;
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

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public List<ViewerModel> getViewers() {
        return viewers;
    }

    public void setViewers(List<ViewerModel> viewers) {
        this.viewers = viewers;
    }

    public List<ViewerModel> getApprover() {
        return approver;
    }

    public void setApprover(List<ViewerModel> approver) {
        this.approver = approver;
    }

    public String getHourType() {
        return hourType;
    }

    public void setHourType(String hourType) {
        this.hourType = hourType;
    }
}
