package com.uniq.tms.tms_microservice.modules.userManagement.projections;

import java.time.LocalDate;

public interface UserProjection {

    String getUserId();
    String getUserName();
    String getEmail();
    String getMobileNumber();
    String getScheduleName();
    String getGroupName();
    String getRoleName();
    String getLocationName();
    LocalDate getDateOfJoining();
    String getSecName();
    String getSecMobile();
    String getSecEmail();
    String getSecRelation();
    String getPolicyId();
    String getPolicyName();
    LocalDate getValidFrom();
    LocalDate getValidTo();
    String getCalendarName();
    String getRequestApproverId();
}
