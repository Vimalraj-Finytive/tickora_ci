package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import com.uniq.tms.tms_microservice.model.WorkScheduleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.Duration;
import java.time.LocalTime;

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
                    try {
                         String startRaw = schedule.getStartTime();
                         String endRaw = schedule.getEndTime();

                         if (startRaw != null && endRaw != null) {
                              LocalTime start = LocalTime.parse(startRaw);
                              LocalTime end = LocalTime.parse(endRaw);

                              schedule.setStartTime(formatLocalTime(start));
                              schedule.setEndTime(formatLocalTime(end));

                              long minutes = Duration.between(start, end).toMinutes();
                              schedule.setDuration(String.format("%02dh %02dm", minutes / 60, minutes % 60));
                         }
                    } catch (Exception e) {
                         System.out.printf("Time format failed for fixed schedule day {}: {}", schedule.getDay(), e.getMessage());
                         schedule.setStartTime(null);
                         schedule.setEndTime(null);
                         schedule.setDuration(null);
                    }
               });
          }

          if (dto.getFlexibleSchedule() != null) {
               dto.getFlexibleSchedule().forEach(flexSchedule -> {
                    if (flexSchedule.getDuration() != null) {
                         try {
                              double raw = Double.parseDouble(flexSchedule.getDuration());
                              flexSchedule.setDuration(formatDuration(raw));
                         } catch (NumberFormatException e) {
                              flexSchedule.setDuration(null);
                         }
                    }
               });
          }

          return dto;
     }

     default String formatLocalTime(LocalTime time) {
          if (time == null) return null;
          return String.format("%02dh %02dm", time.getHour(), time.getMinute());
     }

     default String formatDuration(Double duration) {
          if (duration == null) return null;
          int hours = duration.intValue();
          int minutes = (int) Math.round((duration - hours) * 60);
          return String.format("%02dh %02dm", hours, minutes);
     }

     WorkSchedule toModel(WorkScheduleDto workScheduleDto);

     WorkScheduleTypeDto toTypeDto(WorkScheduleType workScheduleType);
}
