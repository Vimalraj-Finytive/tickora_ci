package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffRequestEntity;

public class TimeoffRequestUserModel {

    private TimeoffRequestEntity request;
    private String userName;

    public TimeoffRequestUserModel(TimeoffRequestEntity request, String userName) {
        this.request = request;
        this.userName = userName;
    }
    public TimeoffRequestEntity getRequest() {
        return request;
    }

    public void setRequest(TimeoffRequestEntity request) {
        this.request = request;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
