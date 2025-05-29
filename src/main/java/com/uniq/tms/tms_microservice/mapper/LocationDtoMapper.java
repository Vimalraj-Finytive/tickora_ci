package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.LocationDto;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationDtoMapper {

    @Mapping(target = "orgId", source = "organizationEntity.organizationId")
    @Mapping(target = "locationId", source = "locationId")
    Location toLocationDTO(LocationEntity locationEntity) ;
    LocationDto toDto(Location location);
}
