package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.WorkScheduleAapter;
import com.uniq.tms.tms_microservice.mapper.WorkScheduleEntityMapper;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import com.uniq.tms.tms_microservice.service.WorkScheduleService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private final WorkScheduleAapter workScheduleAdapter;
    private final WorkScheduleEntityMapper workScheduleEntityMapper;

    public WorkScheduleServiceImpl(WorkScheduleAapter workScheduleAdapter, WorkScheduleEntityMapper workScheduleEntityMapper) {
        this.workScheduleAdapter = workScheduleAdapter;
        this.workScheduleEntityMapper = workScheduleEntityMapper;
    }

    public List<WorkSchedule> getAllWorkSchedules(Long orgId) {
        List<WorkSchedule> workSchedules = workScheduleAdapter.getWorkSchedule(orgId).stream()
                .map(workScheduleEntityMapper::toMiddleware)
                .toList();
        return workSchedules;
    }
}
