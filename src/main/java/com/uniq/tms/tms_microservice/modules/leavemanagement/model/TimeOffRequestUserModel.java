package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ViewerType;

public class TimeOffRequestUserModel {
    private TimeOffRequestEntity request;
    private String userId;
    private String userName;
    private ViewerType type;

    public TimeOffRequestEntity getRequest() {
        return request;
    }

    public TimeOffRequestUserModel(TimeOffRequestEntity request, String userId, String userName, ViewerType type) {
        this.request = request;
        this.userId = userId;
        this.userName = userName;
        this.type = type;
    }

    public TimeOffRequestUserModel(TimeOffRequestEntity request, String userName) {
        this.request = request;
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRequest(TimeOffRequestEntity request) {
        this.request = request;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ViewerType getType() {
        return type;
    }

    public void setType(ViewerType type) {
        this.type = type;
    }
}
