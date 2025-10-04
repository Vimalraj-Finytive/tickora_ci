package com.uniq.tms.tms_microservice.modules.locationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.mapper.LocationDtoMapper;
import com.uniq.tms.tms_microservice.modules.locationManagement.model.Location;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.LocationRepository;
import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationCacheService;
import com.uniq.tms.tms_microservice.shared.dto.CachedData;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocationCacheServiceImpl implements LocationCacheService {

    private final Map<PrivilegeConstants, String> privilegeMap = new ConcurrentHashMap<>();

    private static final Logger log = LogManager.getLogger(LocationCacheServiceImpl.class);
    private final LocationRepository locationRepository;
    private final LocationDtoMapper locationDtoMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheKeyUtil cacheKeyUtil;

    public LocationCacheServiceImpl(LocationRepository locationRepository, LocationDtoMapper locationDtoMapper, @Nullable RedisTemplate<String, Object> redisTemplate, CacheKeyUtil cacheKeyUtil) {
        this.locationRepository = locationRepository;
        this.locationDtoMapper = locationDtoMapper;
        this.redisTemplate = redisTemplate;
        this.cacheKeyUtil = cacheKeyUtil;
    }

    LocalDateTime now = LocalDateTime.now();


    /**
     *
     * @param orgId
     * @return location based on organization from db and cache it redis cache using keys
     */
    public CompletableFuture<List<Location>> loadLocationTable(String orgId, String schema) {

        log.info("Current location tenant:{}", schema);
        String redisKey = cacheKeyUtil.getLocationKey(orgId,schema);

        try{
            List<Location> locations = locationRepository.findByOrgId(orgId).stream()
                    .map(locationDtoMapper::toLocationDTO)
                    .toList();
            if (!locations.isEmpty()) {
                try {
                    if (redisTemplate != null) {
                        CachedData<Location> locationCache = new CachedData<>(locations, now);
                        redisTemplate.opsForValue().set(redisKey, locationCache);
                        log.info("Locations table loaded into cache");
                    } else {
                        log.warn("RedisTemplate is null, skipping cache write for key: {}", redisKey);
                    }
                } catch (Exception e) {
                    log.warn("Redis not available, skipping cache write: {}", e.getMessage());
                }
            }
            else{
                log.warn("No locations found in DB while loading to cache.");
            }
            log.info("Locations return from DB");
            return CompletableFuture.completedFuture(locations);
        } catch (Exception e) {
            log.error("Failed to load locations from DB: ", e);
            throw new RuntimeException("DB fetch failed", e);
        }
    }
}
