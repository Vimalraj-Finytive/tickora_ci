package com.uniq.tms.tms_microservice.modules.timesheetManagement.projection;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogFrom;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogType;

public interface TimesheetHistoryProjection {
    Long getTimesheetId();
    Long getTimesheetHistoryId();
    String getLocationName();
    String getLogTime();
    LogType getLogType();
    LogFrom getLogFrom();
}
