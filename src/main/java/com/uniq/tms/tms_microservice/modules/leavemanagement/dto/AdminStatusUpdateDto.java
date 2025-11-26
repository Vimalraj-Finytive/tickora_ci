package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;

import java.time.LocalDate;

public class AdminStatusUpdateDto {

    private String userId;
    private String policyId;
    private Status status;
    private LocalDate requestDate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }
}
