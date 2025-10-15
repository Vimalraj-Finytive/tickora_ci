package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CountryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Calendar;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CalendarId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CalendarEntityMapper {
    @Mapping(target = "countryEntity", expression = "java(mapCountryEntity(calendarMiddleware.getCountryId()))")
    CalendarEntity toEntity(Calendar calendarMiddleware);

    @Mapping(target = "countryId", source = "countryEntity.id")
    @Mapping(target = "countryCode", source = "countryEntity.code")
    @Mapping(target = "countryName", source = "countryEntity.name")
    @Mapping(target = "id", source = "id")
    Calendar toModel(CalendarEntity savedEntity);

    default List<CalendarEntity> toEntity(CalendarId ids) {
        if (ids == null || ids.getCalendarIds() == null) {
            return null;
        }

        return ids.getCalendarIds().stream()
                .map(this::mapSingleIdToEntity)
                .collect(Collectors.toList());
    }

    default CalendarEntity mapSingleIdToEntity(String id) {
        if (id == null) {
            return null;
        }
        CalendarEntity entity = new CalendarEntity();
        entity.setId(id);
        return entity;
    }

    default CountryEntity mapCountryEntity(String countryId) {
        if (countryId == null || countryId.trim().isEmpty()) {
            return null;
        }
        CountryEntity country = new CountryEntity();
        country.setId(countryId);
        return country;
    }

}
