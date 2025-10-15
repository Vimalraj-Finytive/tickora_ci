package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarIdDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarResponseDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarHolidayEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ImportType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Calendar;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CalendarId;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.LocalDate;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface CalendarDtoMapper {

    CalendarDto toDto(Calendar middleware);

    @Mapping(target = "importType", expression = "java(mapImportType(calendarDto.getImportType()))")
    @Mapping(target = "countryName", ignore = true)
    Calendar toMiddleware(CalendarDto calendarDto);

    @Mapping(target = "year", expression = "java(extractYear(dto.getDate()))")
    @Mapping(target = "calendar", expression = "java(calendarEntity)")
    CalendarHolidayEntity toCalendarHolidayEntity(HolidayDto dto, @Context CalendarEntity calendarEntity);

    default ImportType mapImportType(ImportType importType) {
        return Optional.ofNullable(importType)
                .orElse(ImportType.AUTO);
    }

    default String extractYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.valueOf(date.getYear());
    }

    CalendarId toModel(CalendarIdDto ids);

    CalendarResponseDto toResponseDto(Calendar model);

    default CalendarResponseDto safeToResponseDto(Calendar model) {
        if (model == null) {
            return null;
        }

        return toResponseDto(model);
    }
}
