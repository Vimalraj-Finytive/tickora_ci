package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class RequestApproverDto {

    @NotNull(message = "requestId cannot be null")
    @NotEmpty(message = "requestId cannot be empty")
    private String requestId;
    @NotNull(message = "userId list cannot be null")
    @NotEmpty(message = "userId list cannot be empty")
    private List<String> userId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }
}
