package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.LocationDto;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.model.Location;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LocationEntityMapper {
    @Mapping(target = "organizationEntity", ignore = true)
    LocationEntity toEntity(Location location);

    Location toModel(LocationDto locationDto);

    @Mapping(target = "orgId", source = "organizationEntity.organizationId")
    @Mapping(target = "locationId", source = "locationId")
    Location toDto(LocationEntity savedEntity);

    @AfterMapping
    default void setOrganizationEntity(Location location, @MappingTarget LocationEntity locationEntity) {
        if (location.getOrgId() != null) {
            OrganizationEntity organization = new OrganizationEntity();
            organization.setOrganizationId(location.getOrgId());
            locationEntity.setOrganizationEntity(organization);
        }
    }
    Location toModel(LocationEntity entity);
}
