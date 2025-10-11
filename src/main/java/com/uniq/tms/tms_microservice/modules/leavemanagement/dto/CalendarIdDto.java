package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import java.util.List;

public class CalendarIdDto {
    List<String> calendarIds;

    public List<String> getCalendarIds(){
        return calendarIds;
    }

    public void setCalendarIds(List<String> calendarIds){
        this.calendarIds = calendarIds;
    }
}
