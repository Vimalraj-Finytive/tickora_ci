package com.uniq.tms.tms_microservice.modules.leavemanagement.projection;

import java.sql.Time;
import java.time.LocalDate;

public interface TimeOffExportView {
    String getCreatorId();
    String getCreatorName();
    String getPolicyId();
    String getPolicyName();
    String getRequestedDate();
    LocalDate getLeaveStartDate();
    LocalDate getLeaveEndDate();
    String getLeaveStartTime();
    String getLeaveEndTime();
    String getLeaveType();
    String getStatus();
    String getReason();
    String getViewerId();
    String getViewerType();
    String getViewerName();
    Integer getUnitsRequested();
}
