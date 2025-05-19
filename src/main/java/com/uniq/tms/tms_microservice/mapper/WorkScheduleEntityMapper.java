package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkScheduleEntityMapper {
    WorkSchedule toMiddleware(WorkScheduleEntity entity);
}
