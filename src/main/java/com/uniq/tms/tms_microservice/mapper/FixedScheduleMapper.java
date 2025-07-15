package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.FixedScheduleDto;
import com.uniq.tms.tms_microservice.entity.FixedWorkScheduleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FixedScheduleMapper {
    @Mapping(source = "day", target = "day")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    FixedScheduleDto toDto(FixedWorkScheduleEntity entity);

}
