package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.PublicHolidayEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Holiday;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDate;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HolidayDtoMapper {

    HolidayDto toDto(PublicHolidayEntity publicHolidayEntity);

    HolidayDto toDto(Holiday holiday);

    @Mapping(target = "country.code", ignore = true)
    @Mapping(target = "year" , expression = "java(extractYear(holidayDto.getDate()))")
    PublicHolidayEntity toPublicHolidayEntity(HolidayDto holidayDto);

    default String extractYear(LocalDate date){
        if(date!=null){
            return String.valueOf(date.getYear());
        }
        return null;
    }

    Holiday toMiddleware(HolidayDto holidayDto);
}
