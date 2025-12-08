package com.uniq.tms.tms_microservice.modules.userManagement.model;

import java.util.List;

public class RequestApproverModel {
    private String requestId;
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
