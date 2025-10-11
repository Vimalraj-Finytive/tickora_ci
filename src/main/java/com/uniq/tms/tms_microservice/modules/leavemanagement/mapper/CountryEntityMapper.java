package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CountryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Country;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CountryEntityMapper {
    Country toModel(CountryEntity entity);
}
