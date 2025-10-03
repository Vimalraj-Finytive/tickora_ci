package com.uniq.tms.tms_microservice.modules.locationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.adapter.LocationAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationListDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.mapper.LocationEntityMapper;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.LocationList;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.LocationRepository;
import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationCacheService;
import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.dto.CachedData;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.shared.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheKeyConfig;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.shared.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.shared.util.CacheEventPublisherUtil;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger log = LogManager.getLogger(LocationServiceImpl.class);

    private final CacheKeyUtil cacheKeyUtil;
    private final LocationCacheService locationCacheService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocationEntityMapper locationEntityMapper;
    private final LocationAdapter locationAdapter;
    private final LocationRepository locationRepository;
    private final OrganizationAdapter organizationAdapter;
    private final ApplicationEventPublisher publisher;
    private final CacheKeyConfig cacheKeyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;
    private final UserAdapter userAdapter;

    public LocationServiceImpl(CacheKeyUtil cacheKeyUtil, LocationCacheService locationCacheService, @Nullable RedisTemplate<String, Object> redisTemplate, LocationEntityMapper locationEntityMapper, LocationAdapter locationAdapter, LocationRepository locationRepository, OrganizationAdapter organizationAdapter, ApplicationEventPublisher applicationEventPublisher, CacheKeyConfig cacheKeyConfig, CacheReloadHandlerRegistry cacheReloadHandlerRegistry, UserAdapter userAdapter) {
        this.cacheKeyUtil = cacheKeyUtil;
        this.locationCacheService = locationCacheService;
        this.redisTemplate = redisTemplate;
        this.locationEntityMapper = locationEntityMapper;
        this.locationAdapter = locationAdapter;
        this.locationRepository = locationRepository;
        this.organizationAdapter = organizationAdapter;
        this.publisher = applicationEventPublisher;
        this.cacheKeyConfig = cacheKeyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
        this.userAdapter = userAdapter;
    }

    @Value("${cache.redis.enabled}")
    private boolean isRedisEnabled;

    @Override
    public List<Location> getAllLocation(String orgId) {
        String schema = TenantContext.getCurrentTenant();
        String redisKey = cacheKeyUtil.getLocationKey(orgId, schema);
        CachedData<Location> cachedData = null;

        // Only try Redis if redisTemplate is not null
        if (redisTemplate != null) {
            try {
                cachedData = (CachedData<Location>) redisTemplate.opsForValue().get(redisKey);
            } catch (Exception redisException) {
                log.warn("Redis not available or cache fetch failed: {}", redisException.getMessage());
            }
        } else {
            log.warn("RedisTemplate is null, skipping cache fetch for key: {}", redisKey);
        }

        try {
            if (cachedData != null && cachedData.getData() != null) {
                log.info("Cache hit for key orgId: {} locations Cache called", orgId);
                return cachedData.getData();
            }

            log.info("Cache missing for key: locations, DB Called");

            // Load and cache if missing
            List<Location> locations = locationCacheService.loadLocationTable(orgId, schema).get();
            log.info("Total locations fetched from DB: {}", locations.size());
            locations.forEach(loc -> log.info("Location ID: {}, Org ID: {}", loc.getLocationId(), loc.getOrgId()));
            return locations;

        } catch (Exception e) {
            log.error("Error loading data from DB/cache: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public Location addLocation(LocationDto locationDto, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        Location locationModel = locationEntityMapper.toModel(locationDto);
        try {
            locationModel.setOrgId(orgId);
            if (locationModel.isDefault()) {
                locationRepository.resetDefaultLocation(orgId);
            }
            LocationEntity savedEntity = locationAdapter.addLocation(locationModel);
            int roleId = UserRole.SUPERADMIN.getHierarchyLevel();
            List<UserEntity> user = userAdapter.findUserByOrgIdAndRoleId(orgId, roleId);
            if (user == null) {
                throw new RuntimeException("No User found under the role Superadmin for Logged Organization");
            }
            log.info("User List in role Superadmin:{}", user);
            log.info("Adding user to newly created location");
            LocationEntity locations = locationRepository.findById(savedEntity.getLocationId())
                    .orElseThrow(() -> new NoSuchElementException("Location not found with ID: " + savedEntity.getLocationId()));
            List<UserLocationEntity> userLocationEntities = new ArrayList<>();
            for (UserEntity u : user) {
                UserLocationEntity userLocation = new UserLocationEntity();
                userLocation.setUser(u);
                userLocation.setLocation(locations);
                userLocationEntities.add(userLocation);
                log.info("USERLOCATION:{}", userLocation);
            }
            locationAdapter.saveUserLocation(userLocationEntities);
            if (isRedisEnabled) {
                CacheEventPublisherUtil.syncReloadThenPublish(
                        publisher,
                        cacheKeyConfig.getLocation(),
                        orgId,
                        schema,
                        cacheReloadHandlerRegistry
                );
                log.info("LocationCacheReloadEvent published after location Added");
            } else {
                log.info("Redis is not enabled or RedisTemplate is null. Skipping cache add location reload.");
            }
            return locationEntityMapper.toDto(savedEntity);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location '" + locationModel.getName() + "' already exists in this organization");
        }
    }

    @Override
    public List<LocationDto> getUserLocation(String userId) {
        UserEntity user = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserLocationEntity> userLocations = locationAdapter.fetchLocationsForUser(userId);

        if (userLocations.isEmpty()) {
            throw new CommonExceptionHandler.NoUserLocationAssignedException(user.getUserName());
        }

        return userLocations.stream()
                .map(ul -> locationEntityMapper.tolocationDto(ul.getLocation()))
                .toList();
    }

    @Override
    public ApiResponse updateLocation(String orgId, LocationList location) {
        String schema = TenantUtil.getCurrentTenant();
        List<Long> locationIds = location.getLocationId();
        if (locationIds == null || locationIds.isEmpty()) {
            throw new RuntimeException("No location IDs provided");
        }
        if (location.isDefault() && location.getLocationId().size() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only one Location can be marked as Default"
            );
        }
        Long defaultLocationId = location.isDefault() ? locationIds.get(0) : null;
        if (location.isDefault()) {
            locationRepository.resetDefaultLocation(orgId, defaultLocationId);
        }
        List<LocationEntity> updatedEntities = new ArrayList<>();
        int count = 0;
        for (Long id : locationIds) {
            LocationEntity entity = locationAdapter.findLocationById(id, orgId);
            if (entity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No Location Found for the provided Location Id");
            }
            if (location.getName() != null) entity.setName(location.getName());
            if (location.getAddress() != null) entity.setAddress(location.getAddress());
            if (location.getLatitude() != null) entity.setLatitude(location.getLatitude());
            if (location.getLongitude() != null) entity.setLongitude(location.getLongitude());
            if (location.getRadius() != null) entity.setRadius(location.getRadius());
            entity.setDefault(location.isDefault() && id.equals(defaultLocationId));
            OrganizationEntity organization = new OrganizationEntity();
            organization.setOrganizationId(orgId);
            entity.setOrganizationEntity(organization);
            updatedEntities.add(entity);
            count++;
        }

        List<LocationEntity> savedEntities = locationAdapter.updateMultipleLocations(updatedEntities);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getLocation(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("LocationCacheReloadEvent published after bulk update");
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache update Location reload.");
        }
        String message = String.format("%d locations updated successfully", count);
        return new ApiResponse(200, message, null);
    }

    @Override
    @Transactional
    public void deleteLocation(LocationListDto locationDto, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        List<Long> locationIdList = locationDto.getLocationId();

        boolean exist = locationRepository.existsByLocationIdInAndOrganizationEntity_OrganizationId(locationIdList, orgId);
        if (!exist) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Location not found or access denied");
        }

        Optional<LocationEntity> defaultLocationExist = locationAdapter.findAllDefaultLocationById(locationIdList, orgId);
        if (defaultLocationExist.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete default location");
        }

        LocationEntity defaultLocation = locationAdapter.findDefaultLocationByOrgId(orgId);
        if (defaultLocation == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No default location exists for this organization");
        }

        Long defaultLocationId = defaultLocation.getLocationId();

        List<GroupEntity> groupsToUpdate = locationAdapter.findByLocation_LocationIdIn(locationIdList);
        for (GroupEntity group : groupsToUpdate) {
            if (!group.getLocationEntity().getLocationId().equals(defaultLocationId)) {
                group.setLocationEntity(defaultLocation);
            }
        }
        locationAdapter.saveAllGroups(groupsToUpdate);

        List<UserLocationEntity> userLocationsToUpdate = locationAdapter.findUserLocationByLocationId(locationIdList);

        log.info("User-Location entries to process: {}", userLocationsToUpdate.size());
        if (userLocationsToUpdate.isEmpty()) return;

        List<UserLocationEntity> userLocationsToDelete = new ArrayList<>();
        List<UserLocationEntity> newUserLocationsToInsert = new ArrayList<>();

        for (UserLocationEntity userLoc : userLocationsToUpdate) {
            String userId = userLoc.getUser().getUserId();
            Long currentLocationId = userLoc.getLocation().getLocationId();
            log.info("User Current Location: {}", currentLocationId);
            if (currentLocationId.equals(defaultLocationId)) {
                userLocationsToDelete.add(userLoc);
                continue;
            }
            log.info("Find user allowed locations by userId");
            boolean hasDefaultLocation = locationAdapter
                    .findUserLocationByUserId(userId)
                    .stream()
                    .map(loc -> loc.getLocation().getLocationId())
                    .anyMatch(id -> id.equals(defaultLocationId));
            log.info("hasDefaultLocation : {} ", hasDefaultLocation);
            if (!hasDefaultLocation) {
                UserLocationEntity newUserLoc = new UserLocationEntity();
                newUserLoc.setUser(userLoc.getUser());
                newUserLoc.setLocation(defaultLocation);
                newUserLocationsToInsert.add(newUserLoc);
            }
            userLocationsToDelete.add(userLoc);
        }

        if (!newUserLocationsToInsert.isEmpty()) {
            locationAdapter.saveAllUserLocation(newUserLocationsToInsert);
        }

        locationAdapter.deleteAllUserLocations(userLocationsToDelete);

        log.info("Successfully reassigned {} users to default location and deleted {} old mappings.",
                newUserLocationsToInsert.size(), userLocationsToDelete.size());

        locationAdapter.deleteLocation(locationIdList, orgId);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getLocation(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("Location deleted and references updated. Cache reloaded.");
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache delete location reload.");
        }
    }
    
}
