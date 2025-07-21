package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.WorkScheduleDto;
import com.uniq.tms.tms_microservice.dto.WorkScheduleTypeDto;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import com.uniq.tms.tms_microservice.model.WorkScheduleType;
import java.util.List;

public interface WorkScheduleService {
    List<WorkScheduleDto> getAllWorkSchedules(String orgId);
    ApiResponse createWorkSchedule(WorkSchedule model, String orgId);
    ApiResponse addType(WorkScheduleTypeDto type);
    void updateWorkSchedule(WorkSchedule model, String orgId);
    void deleteWorkSchedule(String orgId, String scheduleId);
    List<WorkScheduleType> getAllTypes();
}
