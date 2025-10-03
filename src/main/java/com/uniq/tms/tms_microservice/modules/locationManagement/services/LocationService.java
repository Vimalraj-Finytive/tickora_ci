package com.uniq.tms.tms_microservice.modules.locationManagement.services;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationListDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.LocationList;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;

import java.util.List;

public interface LocationService {
    List<Location> getAllLocation(String orgId);
    Location addLocation(LocationDto locationDto, String orgId);
    List<LocationDto> getUserLocation(String userId);
    ApiResponse updateLocation(String orgId, LocationList location);
    void deleteLocation(LocationListDto locationIds, String orgId);
}
