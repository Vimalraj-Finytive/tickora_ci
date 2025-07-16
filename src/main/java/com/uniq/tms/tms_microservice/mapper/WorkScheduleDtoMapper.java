package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import com.uniq.tms.tms_microservice.model.WorkScheduleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.sql.Time;

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
                         // Convert ISO time string to Time object
                         String startRaw = schedule.getStartTime();
                         String endRaw = schedule.getEndTime();

                         Time start = Time.valueOf(startRaw.substring(11, 16) + ":00");
                         Time end = Time.valueOf(endRaw.substring(11, 16) + ":00");

                         // Format start and end time to hh mm
                         schedule.setStartTime(formatTimeToHhMm(start));
                         schedule.setEndTime(formatTimeToHhMm(end));

                         // Calculate and format duration
                         double duration = (end.getTime() - start.getTime()) / (1000.0 * 60 * 60);
                         schedule.setDuration(formatDuration(duration));
                    } catch (Exception e) {
                         // Log if needed
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
                              flexSchedule.setDuration(formatDuration(raw)); // formatted string like 07h 30m
                         } catch (NumberFormatException e) {
                              flexSchedule.setDuration(null);
                         }
                    }
               });
          }

          return dto;
     }

     default String formatTimeToHhMm(Time time) {
          if (time == null) return null;
          int hour = time.toLocalTime().getHour();
          int minute = time.toLocalTime().getMinute();
          return String.format("%02dh %02dm", hour, minute);
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
