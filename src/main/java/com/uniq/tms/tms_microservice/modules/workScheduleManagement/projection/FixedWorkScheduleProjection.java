package com.uniq.tms.tms_microservice.modules.workScheduleManagement.projection;

import java.time.LocalTime;

public interface FixedWorkScheduleProjection {
    String getFixedWorkScheduleId();
    String getUserId();
    String getDay();
    LocalTime getStartTime();
    LocalTime getEndTime();
    Double getDuration();
    String getWorkScheduleId();
}
