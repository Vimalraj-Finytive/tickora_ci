package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CountryDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Country;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CountryDtoMapper {
    CountryDto toDto(Country middleware);
}
