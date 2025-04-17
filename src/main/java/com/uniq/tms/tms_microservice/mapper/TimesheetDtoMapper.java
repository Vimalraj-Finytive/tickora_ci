package com.uniq.tms.tms_microservice.mapper;


import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TimesheetDtoMapper {

    TimesheetDto toDto(TimesheetEntity entity);

    TimesheetHistoryDto toDto(TimesheetHistory middleware);

    TimesheetHistory toMiddleware(TimesheetHistoryDto dto);


}
