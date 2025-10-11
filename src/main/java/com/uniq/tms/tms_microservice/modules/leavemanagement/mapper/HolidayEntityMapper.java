package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarHolidayEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Holiday;
import org.mapstruct.Mapper;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface HolidayEntityMapper {

    Holiday toModel(CalendarHolidayEntity entity);

    default List<Holiday> toModelList(List<CalendarHolidayEntity> entities) {
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    CalendarHolidayEntity toEntity(Holiday holidayMiddleware);
}

