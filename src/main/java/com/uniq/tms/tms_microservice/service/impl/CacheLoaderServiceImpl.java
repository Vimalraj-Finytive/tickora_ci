package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.dto.CachedData;
import com.uniq.tms.tms_microservice.dto.PrivilegeConstants;
import com.uniq.tms.tms_microservice.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.mapper.LocationDtoMapper;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.repository.LocationRepository;
import com.uniq.tms.tms_microservice.repository.PrivilegeRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.repository.UserRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class CacheLoaderServiceImpl implements CacheLoaderService {

    private final Map<PrivilegeConstants, String> privilegeMap = new ConcurrentHashMap<>();

    private static final Logger log = LogManager.getLogger(CacheLoaderServiceImpl.class);
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final LocationDtoMapper locationDtoMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    public CacheLoaderServiceImpl(UserRepository userRepository, LocationRepository locationRepository, LocationDtoMapper locationDtoMapper, RedisTemplate<String, Object> redisTemplate, RoleRepository roleRepository, PrivilegeRepository privilegeRepository) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.locationDtoMapper = locationDtoMapper;
        this.redisTemplate = redisTemplate;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @Value("${CACHE_LOCATION}")
    private String location;
    @Value("${CACHE_USER}")
    private String users;
    @Value("${CACHE_ROLE_PRIVILEGE}")
    private String roleprivilege;

    LocalDateTime now = LocalDateTime.now();

    @Async
    public void loadUserTable() {
        try {
            // Load users table into cache using users Key
            List<UserEntity> usersFromDb = userRepository.findAll();
            CachedData<UserEntity> UsersCache = new CachedData<>(usersFromDb, now);
            redisTemplate.opsForValue().set(users, UsersCache);
            log.info("Users table loaded into cache");
        } catch (Exception e) {
            log.error("Failed to load cache: ", e);
            throw new RuntimeException("Cache loading failed", e);
        }
    }

    // Load location table into cache using location Key
    @Async
    public CompletableFuture<List<Location>> loadLocationTable() {
        try{
            List<Location> locations = locationRepository.findAll().stream()
                    .map(locationDtoMapper::toLocationDTO)
                    .toList();;
            if(! locations.isEmpty()) {
                try {
                    CachedData<Location> locationCache = new CachedData<>(locations, now);
                    redisTemplate.opsForValue().set(location, locationCache);
                    log.info("Locations table loaded into cache");
                } catch (Exception e) {
                    log.warn("Redis not available, skipping cache write: {}", e.getMessage());                        }
            }else{
                log.warn("No locations found in DB while loading to cache.");
            }
            log.info("Locations return from DB");
            return CompletableFuture.completedFuture(locations);
        } catch (Exception e) {
            log.error("Failed to load locations from DB: ", e);
            throw new RuntimeException("DB fetch failed", e);
        }
    }

    @Async
    public void loadAllRolesToCache() {
       List<RoleEntity> roles = roleRepository.findAllWithPrivileges();

        Map<String, Set<String>> rolePrivileges = new HashMap<>();
        for (RoleEntity role : roles) {
            Set<String> privileges = role.getPrivilegeEntities()
                    .stream()
                    .map(PrivilegeEntity::getName)
                    .collect(Collectors.toSet());
            rolePrivileges.put(role.getName(), privileges);
        }

        rolePrivileges.forEach((role, privileges) -> {
            redisTemplate.opsForValue().set(roleprivilege + role.toLowerCase(), privileges);
            log.info("Loaded role {} with privileges: {}", role, privileges);
        });
    }

    public String getPrivilegeKey(PrivilegeConstants constant) {
        return privilegeMap.get(constant);
    }

    @Async
    public void loadPrivilegesFromDB() {
        List<PrivilegeEntity> privileges = privilegeRepository.findAll();
        log.info("Loading privileges from DB: {}", privileges.size());

        for (PrivilegeConstants constant : PrivilegeConstants.values()) {
            log.info("Loading privilege from Enum: {}", constant.name());
            privileges.stream()
                    .filter(p -> p.getStaticName().equalsIgnoreCase(constant.name()))
                    .findFirst()
                    .ifPresentOrElse(
                            p -> privilegeMap.put(constant, p.getName()),
                            () -> log.warn("Privilege NOT FOUND in DB for constant: {}", constant.name())
                    );
        }

        // Log all mapped privileges
        privilegeMap.forEach((key, value) ->
                log.info("Privilege constant: {}, Privilege name: {}", key, value));
    }
}
