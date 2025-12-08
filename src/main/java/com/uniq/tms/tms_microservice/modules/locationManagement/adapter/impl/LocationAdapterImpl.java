package com.uniq.tms.tms_microservice.modules.locationManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.adapter.LocationAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.mapper.LocationEntityMapper;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.LocationRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.GroupRepository;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.UserLocationRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LocationAdapterImpl implements LocationAdapter {

    private static final Logger log = LogManager.getLogger(LocationAdapterImpl.class);

    private final LocationRepository locationRepository;
    private final UserLocationRepository userLocationRepository;
    private final GroupRepository groupRepository;
    private final LocationEntityMapper locationEntityMapper;

    public LocationAdapterImpl(LocationRepository locationRepository, UserLocationRepository userLocationRepository, GroupRepository groupRepository, LocationEntityMapper locationEntityMapper) {
        this.locationRepository = locationRepository;
        this.userLocationRepository = userLocationRepository;
        this.groupRepository = groupRepository;
        this.locationEntityMapper = locationEntityMapper;
    }

    @Override
    public List<LocationEntity> findAllLocationById(List<Long> locationIds) {
        return locationRepository.findByLocationIdInAndActiveTrue(locationIds);
    }

    @Override
    public List<LocationEntity> updateMultipleLocations(List<LocationEntity> updatedEntities) {
        return locationRepository.saveAll(updatedEntities);
    }

    @Override
    public Optional<LocationEntity> findAllDefaultLocationById(List<Long> locationIds, String orgId) {
        return locationRepository.findDefaultLocationByOrgId(locationIds, orgId);
    }

    @Override
    public List<GroupEntity> findByLocation_LocationIdIn(List<Long> defaultLocationId) {
        return groupRepository.findByLocationEntity_LocationIdIn(defaultLocationId);
    }

    @Override
    public void saveAllGroups(List<GroupEntity> groupsToUpdate) {
        groupRepository.saveAll(groupsToUpdate);
    }

    @Override
    public List<UserLocationEntity> findUserLocationByLocationId(List<Long> defaultLocationId) {
        return  userLocationRepository.findByLocation_LocationIdIn(defaultLocationId);
    }

    @Override
    public void saveAllUserLocation(List<UserLocationEntity> newUserLocationsToInsert) {
        userLocationRepository.saveAll(newUserLocationsToInsert);
    }

    @Override
    @Transactional
    public void deleteAllUserLocations(List<UserLocationEntity> userLocationsToDelete) {
        log.info("Attempting to delete {} user-location mappings", userLocationsToDelete.size());

        userLocationsToDelete.forEach(ul ->
                log.info("Deleting mapping: userId={}, locationId={}",
                        ul.getUser().getUserId(), ul.getLocation().getLocationId()));

        userLocationRepository.deleteAll(userLocationsToDelete);

        log.info("DeleteAll executed. Flushing...");
        userLocationRepository.flush();
    }

    @Override
    public List<UserLocationEntity> fetchLocationsForUser(String userId) {
        return userLocationRepository.fetchLocationsForUser(userId);
    }

    @Override
    public LocationEntity addLocation(Location location) {
        LocationEntity locationEntity = locationEntityMapper.toEntity(location);
        return locationRepository.save(locationEntity);
    }

    @Override
    public List<LocationEntity> findLocation(String orgId) {
        return locationRepository.findLocationByOrganizationEntity_OrganizationIdAndActiveTrue(orgId);
    }

    @Override
    public void deleteLocation(List<Long> locationIds, String orgId) {
        locationRepository.deleteAllLocationById(locationIds, orgId);
    }

    @Override
    public LocationEntity findDefaultLocationByOrgId(String orgId) {
        return locationRepository.findDefaultLocationById(orgId);
    }

    @Override
    public Optional<LocationEntity> findLocationByLocationId(Long locationId) {
        return locationRepository.findById(locationId);
    }

    @Override
    public List<UserLocationEntity> findByUser_UserId(String userId) {
        return userLocationRepository.findByUser_UserId(userId);
    }

    @Override
    public List<GroupEntity> findGroupLocationByLocationId(List<Long> locationIds) {
        return groupRepository.findByLocationEntity_LocationIdIn(locationIds);
    }

    @Override
    public void saveUserLocation(List<UserLocationEntity> entityList) {
        userLocationRepository.saveAll(entityList);
    }

    @Override
    public List<UserLocationEntity> findUserLocationByUserId(String userId) {
        return userLocationRepository.findByUser_UserId(userId);
    }

    @Override
    @Transactional
    public void deleteUserLocationByUserId(String userId, Set<Long> toDelete) {
        userLocationRepository.deleteByUser_UserIdAndLocation_LocationIdIn(userId, toDelete);
    }

    @Override
    @Transactional
    public void updateUserLocationByUserId(List<UserLocationEntity> newEntities) {
        userLocationRepository.saveAll(newEntities);
    }

    @Override
    public Map<String, Long> getLocationNameToIdMap(String orgId) {
        List<Object[]> locations = locationRepository.findLocationNameIdMappings(orgId);
        Map<String, Long> locationNameToIdMap = new HashMap<>();
        for (Object[] location : locations) {
            locationNameToIdMap.put(((String) location[0]).toLowerCase(), (Long) location[1]);
        }
        return locationNameToIdMap;
    }

    @Override
    public LocationEntity findLocationById(Long locationId, String orgId) {
        return locationRepository.findByLocationIdAndOrganizationEntity_OrganizationIdAndActiveTrue(locationId, orgId);
    }


    @Override
    public List<UserEntity> findMembersByLocationIds(
            List<Long> locationIds, String userIdFromToken) {
        return userLocationRepository.findMembersByLocationIds(locationIds, userIdFromToken);

    }
    @Override
    public List<UserLocationEntity> findUserLocationsByLocationId(List<Long> locationIds) {
        return userLocationRepository.findByLocation_LocationIdIn(locationIds);
    }

    @Override
    public List<UserEntity> findUsersByIdsAndLocationIds(List<String> userIds, List<Long> locationIds) {
        return userLocationRepository.findUsersByIdsAndLocationIds(userIds, locationIds);
    }

    @Override
    public LocationEntity findDefaultLocation() {
        return locationRepository.findByIsDefaultTrue();
    }
}
