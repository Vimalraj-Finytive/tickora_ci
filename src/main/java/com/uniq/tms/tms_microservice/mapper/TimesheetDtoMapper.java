package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetStatusEntity;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.model.TimesheetStatus;
import com.uniq.tms.tms_microservice.projection.TimesheetProjection;
import com.uniq.tms.tms_microservice.projection.UserDashboard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimesheetDtoMapper {

    @Mapping(source = "trackedHours", target = "trackedHours", qualifiedByName = "localTimeToDuration")
    @Mapping(source = "regularHours", target = "regularHours", qualifiedByName = "localTimeToDuration")
    @Mapping(source = "status.statusName", target = "status")
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

    UserDashboardDto toDto(UserDashboard userDashboard);

    TimesheetStatus toStatusModel(TimesheetStatusEntity entity);

    TimesheetStatusDto toStatusDto(TimesheetStatus timesheetStatus);

    List<TimesheetDto> toDto(List<TimesheetProjection> timesheets);
    TimesheetDto toDto(TimesheetProjection projection);

    default Duration map(LocalTime localTime) {
        if (localTime == null) {
            return Duration.ZERO;
        }
        return Duration.ofHours(localTime.getHour())
                .plusMinutes(localTime.getMinute())
                .plusSeconds(localTime.getSecond());
    }
}
