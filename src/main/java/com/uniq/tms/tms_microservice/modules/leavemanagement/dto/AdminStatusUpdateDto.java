package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AdminStatusUpdateDto {
    @NotNull(message = "requestId ID cannot be null")
    private Long requestId;
    @NotNull(message = "User ID cannot be null")
    private String userId;
    @NotNull(message = "Policy ID cannot be null")
    private String policyId;
    private Status status;
    @NotNull(message = "Request date cannot be null")
    private LocalDate requestDate;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

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
