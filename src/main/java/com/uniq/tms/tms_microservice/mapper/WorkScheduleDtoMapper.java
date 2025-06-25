package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.WorkScheduleDto;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkScheduleDtoMapper {
     WorkScheduleDto toDto(WorkSchedule workSchedule) ;
}
