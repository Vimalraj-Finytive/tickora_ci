package com.uniq.tms.tms_microservice.modules.workScheduleManagement.projection;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WeeklyScheduleDto;

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
