package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.model.WorkSchedule;
import java.util.List;

public interface WorkScheduleService {
    List<WorkSchedule> getAllWorkSchedules(Long orgId);
}
