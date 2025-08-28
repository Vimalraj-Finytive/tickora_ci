package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.FixedScheduleDto;
import com.uniq.tms.tms_microservice.dto.FlexibleScheduleDto;
import com.uniq.tms.tms_microservice.dto.WeeklyScheduleDto;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.enums.DayOfWeekEnum;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import com.uniq.tms.tms_microservice.model.WorkScheduleType;
import org.mapstruct.*;

import java.sql.Time;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkScheduleEntityMapper {

    @Mapping(source = "type", target = "type")
    @Mapping(source = "type.type", target = "typeName")
    @Mapping(source = "organizationEntity.organizationId", target = "orgId")
    @Mapping(source = "fixedWorkSchedules", target = "fixedSchedule")
    @Mapping(source = "flexibleWorkSchedules", target = "flexibleSchedule")
    @Mapping(source = "weeklyWorkSchedule", target = "weeklySchedule")
    WorkSchedule toMiddleware(WorkScheduleEntity entity);

    @Mapping(source = "type", target = "type")
    WorkScheduleEntity toEntity(WorkSchedule model);

    WorkScheduleType toModel(WorkScheduleTypeEntity saved);

    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "day", ignore = true)
    FixedWorkScheduleEntity toEntity(FixedScheduleDto dto);

    List<FixedWorkScheduleEntity> toEntity(List<FixedScheduleDto> dtos);

    List<FlexibleWorkScheduleEntity> toFlexibleEntity(List<FlexibleScheduleDto> flexibleSchedule);

    WeeklyWorkScheduleEntity toWeeklyEntity(WeeklyScheduleDto dto);

    @AfterMapping
    default void setDerivedFields(@MappingTarget FixedWorkScheduleEntity entity, FixedScheduleDto dto) {
        Time start = Time.valueOf(dto.getStartTime() + ":00");
        Time end = Time.valueOf(dto.getEndTime() + ":00");
        entity.setStartTime(start);
        entity.setEndTime(end);

        long millis = end.getTime() - start.getTime();
        double duration = Math.round((millis / (1000.0 * 60 * 60)) * 100.0) / 100.0;
        entity.setDuration(duration);
        entity.setDay(DayOfWeekEnum.valueOf(dto.getDay()));
    }

    @AfterMapping
    default void enrichFlexible(@MappingTarget FlexibleWorkScheduleEntity entity, FlexibleScheduleDto dto) {
        entity.setDay(DayOfWeekEnum.valueOf(dto.getDay()));
        entity.setDuration(Double.valueOf(dto.getDuration()));
    }

    @AfterMapping
    default void enrichWeekly(@MappingTarget WeeklyWorkScheduleEntity entity, WeeklyScheduleDto dto) {
        entity.setStartDay(DayOfWeekEnum.valueOf(dto.getStartDay()));
        entity.setEndDay(DayOfWeekEnum.valueOf(dto.getEndDay()));
        entity.setDuration(dto.getDuration());
    }

    default WorkScheduleTypeEntity map(String typeId) {
        if (typeId == null) return null;
        WorkScheduleTypeEntity entity = new WorkScheduleTypeEntity();
        entity.setTypeId(typeId);
        return entity;
    }

    default String map(WorkScheduleTypeEntity typeId) {
        return typeId != null ? typeId.getTypeId() : null ;
    }
}
