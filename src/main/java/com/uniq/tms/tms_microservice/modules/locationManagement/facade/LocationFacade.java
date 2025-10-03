package com.uniq.tms.tms_microservice.modules.locationManagement.facade;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationListDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.mapper.LocationDtoMapper;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.LocationList;
import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class LocationFacade {

    private final LocationService locationService;
    private final LocationDtoMapper locationDtoMapper;
    private final AuthHelper authHelper;

    public LocationFacade(LocationService locationService, LocationDtoMapper locationDtoMapper, AuthHelper authHelper) {
        this.locationService = locationService;
        this.locationDtoMapper = locationDtoMapper;
        this.authHelper = authHelper;
    }

    public ApiResponse getAllLocation(String orgId) {
        try {
            List<LocationDto> locations = locationService.getAllLocation(orgId)
                    .stream()
                    .map(locationDtoMapper::toDto)
                    .toList();
            return new ApiResponse(200, "Locations fetched successfully", locations);
        } catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }

    public ApiResponse addLocation( LocationDto locationDto) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        Location location = locationService.addLocation(locationDto, orgId);
        return new ApiResponse(200, "Location added successfully", location);
    }

    public ApiResponse getUserLocation() {
        String userId = authHelper.getUserId();
        List<LocationDto> locations = locationService.getUserLocation(userId);
        return new ApiResponse(200, "User Location fetched successfully", locations);
    }

    public ApiResponse updateLocation( LocationListDto locationDto) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        LocationList location = locationDtoMapper.toModel(locationDto);
        ApiResponse response = locationService.updateLocation(orgId, location);
        return response;
    }

    public void deleteLocation( LocationListDto locationIds) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }

        locationService.deleteLocation(locationIds, orgId);
    }


}
