package com.uniq.tms.tms_microservice.modules.timesheetManagement.projection;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimesheetUserProjection {
    Long getId();
    String getUserId();
    LocalDate getDate();
    String getUserName();
    LocalTime getFirstClockIn();
    LocalTime getLastClockOut();
    LocalTime getTrackedHours();
    LocalTime getRegularHours();
    String getStatus();
    List<TimesheetHistoryProjection> getHistory();
    String getWorkStatus();
}
