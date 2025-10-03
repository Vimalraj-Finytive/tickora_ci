package com.uniq.tms.tms_microservice.modules.identityManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.adapter.IdGeneratorAdapter;
import com.uniq.tms.tms_microservice.modules.identityManagement.enums.IdGenerationTypeEnum;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.repository.*;
import org.springframework.stereotype.Component;

@Component
public class IdGeneratorAdapterImpl implements IdGeneratorAdapter {

    private final WorkScheduleRepository workScheduleRepository;
    private final WorkScheduleTypeRepository workScheduleTypeRepository;
    private final FlexibleWorkScheduleRepository flexibleWorkScheduleRepository;
    private final FixedWorkScheduleRepository fixedWorkScheduleRepository;
    private final WeeklyWorkScheduleRepository weeklyWorkScheduleRepository;

    public IdGeneratorAdapterImpl(WorkScheduleRepository workScheduleRepository, WorkScheduleTypeRepository workScheduleTypeRepository, FlexibleWorkScheduleRepository flexibleWorkScheduleRepository, FixedWorkScheduleRepository fixedWorkScheduleRepository, WeeklyWorkScheduleRepository weeklyWorkScheduleRepository) {
        this.workScheduleRepository = workScheduleRepository;
        this.workScheduleTypeRepository = workScheduleTypeRepository;
        this.flexibleWorkScheduleRepository = flexibleWorkScheduleRepository;
        this.fixedWorkScheduleRepository = fixedWorkScheduleRepository;
        this.weeklyWorkScheduleRepository = weeklyWorkScheduleRepository;
    }


    public String findMaxIdByPrefix(IdGenerationTypeEnum type, String prefix) {
        return switch (type) {
            case WORK_SCHEDULE -> workScheduleRepository.findMaxIdByPrefix(prefix);
            case WORK_SCHEDULE_TYPE -> workScheduleTypeRepository.findMaxIdByPrefix(prefix);
            case FIXED_WORK -> fixedWorkScheduleRepository.findMaxIdByPrefix(prefix);
            case FLEXIBLE_WORK -> flexibleWorkScheduleRepository.findMaxIdByPrefix(prefix);
            case WEEKLY_WORK -> weeklyWorkScheduleRepository.findMaxIdByPrefix(prefix);
            default -> throw new IllegalArgumentException("Unsupported type for ID generation: " + type);
        };
    }
}
