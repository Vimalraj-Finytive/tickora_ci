package com.uniq.tms.tms_microservice.modules.locationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationListDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.LocationList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationDtoMapper {

    @Mapping(target = "orgId", source = "organizationEntity.organizationId")
    @Mapping(target = "locationId", source = "locationId")
    Location toLocationDTO(LocationEntity locationEntity) ;
    LocationDto toDto(Location location);
    LocationDto toDto(LocationEntity locationEntity);
    LocationList toModel(LocationListDto locationDto);

}
