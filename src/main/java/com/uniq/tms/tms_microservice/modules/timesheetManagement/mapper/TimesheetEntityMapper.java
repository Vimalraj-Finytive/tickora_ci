package com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.model.TimesheetHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimesheetEntityMapper {

    TimesheetHistory toMiddleware(TimesheetHistoryEntity entity);

    @Mapping(target = "timesheet.user.userId", source = "userId")
    @Mapping(target = "timesheet.user.dateOfJoining", source = "date")
    TimesheetHistoryEntity toEntity(TimesheetHistoryDto timesheetHistory);

    TimesheetHistoryDto toDto(TimesheetHistory timesheetHistory);

    TimesheetHistory toMiddleware(TimesheetHistoryDto dto);

}
