package com.uniq.tms.tms_microservice.modules.locationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserLocationEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface LocationAdapter {
    List<LocationEntity> findAllLocationById(List<Long> locationIds);
    List<LocationEntity> updateMultipleLocations(List<LocationEntity> updatedEntities);
    Optional<LocationEntity> findAllDefaultLocationById(List<Long> locationIds, String orgId);
    List<GroupEntity> findByLocation_LocationIdIn(List<Long> defaultLocationId);
    void saveAllGroups(List<GroupEntity> groupsToUpdate);
    List<UserLocationEntity> findUserLocationByLocationId(List<Long> defaultLocationId);
    void saveAllUserLocation(List<UserLocationEntity> newUserLocationsToInsert);
    void deleteAllUserLocations(List<UserLocationEntity> userLocationsToDelete);
    List<UserLocationEntity> fetchLocationsForUser(String userId);
    LocationEntity addLocation(Location location);
    List<LocationEntity> findLocation(String orgId);
    LocationEntity findLocationById(Long locationId, String orgId);
    Map<String, Long> getLocationNameToIdMap(String orgId);
    void saveUserLocation(List<UserLocationEntity> userLocationEntities);
    List<UserLocationEntity> findUserLocationByUserId(String userId);
    void deleteUserLocationByUserId(String userId, Set<Long> toDelete);
    void updateUserLocationByUserId(List<UserLocationEntity>newEntities);
    void deleteLocation(List<Long> locationIds, String orgId);
    LocationEntity findDefaultLocationByOrgId(String orgId);
    List<UserLocationEntity> findByUser_UserId(String userId);
    List<GroupEntity> findGroupLocationByLocationId(List<Long> locationIds);
    Optional<LocationEntity> findLocationByLocationId(Long locationId);
}
