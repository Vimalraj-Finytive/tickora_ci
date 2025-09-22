package com.uniq.tms.tms_microservice.projection;

import com.uniq.tms.tms_microservice.enums.LogFrom;
import com.uniq.tms.tms_microservice.enums.LogType;

public interface TimesheetHistoryProjection {
    Long getTimesheetId();
    Long getTimesheetHistoryId();
    String getLocationName();
    String getLogTime();
    LogType getLogType();
    LogFrom getLogFrom();
}
