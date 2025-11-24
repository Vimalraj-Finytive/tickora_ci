package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;

public class TimeOffRequestUserModel {

    private TimeOffRequestEntity request;
    private String userName;

    public TimeOffRequestUserModel(TimeOffRequestEntity request, String userName) {
        this.request = request;
        this.userName = userName;
    }
    public TimeOffRequestEntity getRequest() {
        return request;
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
}
