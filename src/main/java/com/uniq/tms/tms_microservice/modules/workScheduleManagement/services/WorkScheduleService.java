package com.uniq.tms.tms_microservice.modules.workScheduleManagement.services;

import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WorkScheduleDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WorkScheduleTypeDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.WorkSchedule;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.WorkScheduleType;
import java.util.List;

public interface WorkScheduleService {
    List<WorkScheduleDto> getAllWorkSchedules(String orgId);
    ApiResponse createWorkSchedule(WorkSchedule model, String orgId);
    ApiResponse addType(WorkScheduleTypeDto type);
    void updateWorkSchedule(WorkSchedule model, String orgId);
    void deleteWorkSchedule(String orgId, String scheduleId);
    List<WorkScheduleType> getAllTypes();
}
