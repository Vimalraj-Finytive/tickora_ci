package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimesheetEntityMapper {

    TimesheetHistory toMiddleware(TimesheetHistoryEntity entity);

    @Mapping(target = "timesheet.userId", source = "userId")
    @Mapping(target = "timesheet.date", source = "date")
    TimesheetHistoryEntity toEntity(TimesheetHistoryDto timesheetHistory);

    TimesheetHistoryDto toDto(TimesheetHistory timesheetHistory);

}
