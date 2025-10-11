package com.uniq.tms.tms_microservice.modules.workScheduleManagement.projection;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WeeklyScheduleDto;
import java.sql.Time;

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
    Time getSplitTime();
}
