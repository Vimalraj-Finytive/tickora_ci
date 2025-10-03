package com.uniq.tms.tms_microservice.modules.workScheduleManagement.services;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WorkScheduleDto;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface WorkScheduleCacheService {
    CompletableFuture<Map<String, List<WorkScheduleDto>>> loadWorkSchedule(String orgId, String schema);
}
