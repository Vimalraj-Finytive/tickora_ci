package com.uniq.tms.tms_microservice.projection;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimesheetProjection {

    Long getId();
    String getUserId();
    LocalDate getDate();
    String getUserName();
    String getMobileNumber();
    String getRoleName();
    String getGroupName();
    String getWorkScheduleName();
    LocalTime getFirstClockIn();
    LocalTime getLastClockOut();
    LocalTime getTrackedHours();
    LocalTime getRegularHours();
    String getStatus();
    List<TimesheetHistoryProjection> getHistory();
    String getWorkStatus();
}
