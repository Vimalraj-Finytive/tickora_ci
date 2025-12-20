package com.uniq.tms.tms_microservice.modules.leavemanagement.projection;

import java.time.LocalDate;

public interface TimeOffExportView {
    Long getTimeoffRequestId();
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
    String getHourType();
}
