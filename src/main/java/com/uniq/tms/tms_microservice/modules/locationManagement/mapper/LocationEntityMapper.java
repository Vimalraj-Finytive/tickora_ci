package com.uniq.tms.tms_microservice.modules.locationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import com.uniq.tms.tms_microservice.modules.userManagement.model.UserLocation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
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

    UserLocation toModel(UserLocationEntity userLocationEntity);

    LocationDto tolocationDto(LocationEntity entity);

    @Mapping(target = "orgId", source = "organizationEntity.organizationId")
    @Mapping(target = "locationId", source = "locationId")
    Location toMiddleware(LocationEntity entity);

    Location toMiddleware(LocationDto locationDto);

}
