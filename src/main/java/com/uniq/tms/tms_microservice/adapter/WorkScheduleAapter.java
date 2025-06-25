package com.uniq.tms.tms_microservice.adapter;

import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import java.util.List;

public interface WorkScheduleAapter {
    List<WorkScheduleEntity> getWorkSchedule(Long orgId);
    WorkScheduleEntity findByWorkscheduleId(Long workScheduleId);
    WorkScheduleEntity findDefaultActiveSchedule(Long orgId);
}
