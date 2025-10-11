package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Calendar;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CalendarId;
import org.mapstruct.Mapper;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CalendarEntityMapper {
    CalendarEntity toEntity(Calendar calendarMiddleware);
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

}
