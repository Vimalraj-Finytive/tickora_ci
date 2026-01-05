package com.uniq.tms.tms_microservice.modules.workScheduleManagement.projection;

public interface FlexibleScheduleProjection {
    String getUserId();
    String getDay();
    Double getDuration();
    String getWorkScheduleId();
}
