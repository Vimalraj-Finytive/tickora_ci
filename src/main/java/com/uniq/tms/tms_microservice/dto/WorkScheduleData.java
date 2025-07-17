package com.uniq.tms.tms_microservice.dto;

public interface WorkScheduleData {

    String getWorkScheduleId();
    String getWorkScheduleName();
    Boolean getIsDefault();
    Boolean getIsActive();
    Long getOrganizationId();
    String getWorkScheduleType();
    String getFixedSchedule();
    String getFlexibleSchedule();
    WeeklyScheduleDto getWeeklySchedule();
}
