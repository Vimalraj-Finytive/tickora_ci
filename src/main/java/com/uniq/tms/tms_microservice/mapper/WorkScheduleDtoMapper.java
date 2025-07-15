package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.FixedWorkScheduleEntity;
import com.uniq.tms.tms_microservice.entity.FlexibleWorkScheduleEntity;
import com.uniq.tms.tms_microservice.entity.WeeklyWorkScheduleEntity;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import com.uniq.tms.tms_microservice.model.WorkScheduleType;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface WorkScheduleDtoMapper {

     @Mapping(source = "fixedSchedule", target = "fixedSchedule")
     @Mapping(source = "flexibleSchedule", target = "flexibleSchedule")
     @Mapping(source = "weeklySchedule", target = "weeklySchedule")
     WorkScheduleDto toDto(WorkSchedule workSchedule);

     default WorkScheduleDto toDtoWithFormattedTimes(WorkSchedule workSchedule) {
          WorkScheduleDto dto = toDto(workSchedule);

          if (dto.getFixedSchedule() != null) {
               dto.getFixedSchedule().forEach(schedule -> {
                    if (schedule.getStartTime() != null && schedule.getStartTime().contains("T")) {
                         schedule.setStartTime(schedule.getStartTime().substring(11, 16)); // "HH:mm"
                    }
                    if (schedule.getEndTime() != null && schedule.getEndTime().contains("T")) {
                         schedule.setEndTime(schedule.getEndTime().substring(11, 16)); // "HH:mm"
                    }
               });
          }

          return dto;
     }

     WorkSchedule toModel(WorkScheduleDto workScheduleDto);

     @Mapping(target = "startTime", ignore = true)
     @Mapping(target = "endTime", ignore = true)
     FixedScheduleDto toDto(FixedWorkScheduleEntity entity);

     FlexibleScheduleDto toDto(FlexibleWorkScheduleEntity entity);

     WeeklyScheduleDto toDto(WeeklyWorkScheduleEntity entity);

     @AfterMapping
     default void formatTimes(@MappingTarget FixedScheduleDto dto, FixedWorkScheduleEntity entity) {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
          if (entity.getStartTime() != null) {
               dto.setStartTime(entity.getStartTime().toLocalTime().format(formatter));
          }
          if (entity.getEndTime() != null) {
               dto.setEndTime(entity.getEndTime().toLocalTime().format(formatter));
          }
     }

    WorkScheduleTypeDto toTypeDto(WorkScheduleType workScheduleType);
}
