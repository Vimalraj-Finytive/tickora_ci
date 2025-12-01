package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.util.List;

public class UserCalendarRequestDto {

    List<String> userIds;
    String calendarId;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

}
