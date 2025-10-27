package com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetStatusEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.model.TimesheetStatus;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.projection.TimesheetProjection;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserDashboard;
import org.mapstruct.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    @Named("defaultMapping")
    @Mapping(target = "firstClockInTime", expression = "java(formatTime(projection.getFirstClockIn()))")
    @Mapping(target = "lastClockOutTime", expression = "java(formatTime(projection.getLastClockOut()))")
    TimesheetDto toDto(TimesheetProjection projection);

    @Named("timeOnlyMapping")
    TimesheetDto toTimeDto(TimesheetProjection projection);

    @IterableMapping(qualifiedByName = "defaultMapping")
    List<TimesheetDto> toDto(List<TimesheetProjection> timesheets);

    @IterableMapping(qualifiedByName = "timeOnlyMapping")
    List<TimesheetDto> toTimeDto(List<TimesheetProjection> timesheets);

    @Mapping(source = "orgId", target = "organizationId")
    @Mapping(source = "orgName", target = "orgName")
    @Mapping(source = "presentCount", target = "presentCount")
    @Mapping(source = "absentCount", target = "absentCount")
    DashboardOrganizationSummaryDto toDashboardOrgSummary(String orgId, String orgName, int presentCount, int absentCount);

    DashboardSummaryDto toDashboardSummary(LocalDate date, List<DashboardOrganizationSummaryDto> organizations);


    default String formatTime(LocalTime localTime) {
        if (localTime == null) return "00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return localTime.format(formatter);
    }

    default Duration map(LocalTime localTime) {
        if (localTime == null) {
            return Duration.ZERO;
        }
        return Duration.ofHours(localTime.getHour())
                .plusMinutes(localTime.getMinute())
                .plusSeconds(localTime.getSecond());
    }

}
