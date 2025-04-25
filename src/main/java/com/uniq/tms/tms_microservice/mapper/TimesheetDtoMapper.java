package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import java.time.Duration;
import java.time.LocalTime;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimesheetDtoMapper {

    @Mapping(source = "trackedHours", target = "trackedHours", qualifiedByName = "localTimeToDuration")
    @Mapping(source = "regularHours", target = "regularHours", qualifiedByName = "localTimeToDuration")
    TimesheetDto toDto(TimesheetEntity entity);

    // Custom conversion from LocalTime to Duration
    @Named("localTimeToDuration")
    default Duration localTimeToDuration(LocalTime localTime) {
        if (localTime == null) {
            return Duration.ZERO;
        }
        return Duration.ofHours(localTime.getHour())
                .plusMinutes(localTime.getMinute())
                .plusSeconds(localTime.getSecond());
    }

    TimesheetHistoryDto toDto(TimesheetHistory middleware);

    TimesheetHistory toMiddleware(TimesheetHistoryDto dto);
}
