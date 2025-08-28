package com.uniq.tms.tms_microservice.projection;

import com.uniq.tms.tms_microservice.dto.WeeklyScheduleDto;

public interface WorkScheduleData {

    String getWorkScheduleId();
    String getWorkScheduleName();
    Boolean getIsDefault();
    Boolean getIsActive();
    String getOrganizationId();
    String getWorkScheduleType();
    String getFixedSchedule();
    String getFlexibleSchedule();
    WeeklyScheduleDto getWeeklySchedule();
}
