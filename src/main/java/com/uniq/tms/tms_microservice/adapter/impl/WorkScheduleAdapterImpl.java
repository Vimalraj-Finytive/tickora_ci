package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.WorkScheduleAapter;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.repository.WorkScheduleRepository;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class WorkScheduleAdapterImpl implements WorkScheduleAapter {

    private final WorkScheduleRepository workScheduleRepository;

    public WorkScheduleAdapterImpl(WorkScheduleRepository workScheduleRepository) {
        this.workScheduleRepository = workScheduleRepository;
    }

    public List<WorkScheduleEntity> getWorkSchedule(Long orgId) {
        return workScheduleRepository.findAllByOrganizationEntity_OrganizationId(orgId);
    }

    @Override
    public WorkScheduleEntity findByWorkscheduleId(Long workScheduleId) {
        return workScheduleRepository.findByScheduleId(workScheduleId);
    }

    @Override
    public WorkScheduleEntity findDefaultActiveSchedule(Long orgId) {
        return workScheduleRepository.findDefaultActiveSchedule(orgId);
    }
}
