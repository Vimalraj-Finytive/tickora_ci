package com.uniq.tms.tms_microservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.config.security.cache.CacheKeyConfig;
import com.uniq.tms.tms_microservice.config.security.cache.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.enums.IdGenerationType;
import com.uniq.tms.tms_microservice.enums.WorkScheduleTypeEnum;
import com.uniq.tms.tms_microservice.mapper.WorkScheduleEntityMapper;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import com.uniq.tms.tms_microservice.model.WorkScheduleType;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.IdGenerationService;
import com.uniq.tms.tms_microservice.service.WorkScheduleService;
import com.uniq.tms.tms_microservice.util.CacheEventPublisherUtil;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private static final Logger log = LogManager.getLogger(WorkScheduleServiceImpl.class);
    @PersistenceContext
    private EntityManager entityManager;

    @Value("${cache.redis.enabled:false}")
    private boolean isRedisEnabled;

    private final WorkScheduleAdapter workScheduleAdapter;
    private final WorkScheduleEntityMapper workScheduleEntityMapper;
    private final IdGenerationService idGenerationService;
    private final UserAdapter userAdapter;
    private final CacheKeyUtil cacheKeyUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheLoaderService cacheLoaderService;
    private final ApplicationEventPublisher publisher;
    private final CacheKeyConfig cacheKeyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;

    public WorkScheduleServiceImpl(WorkScheduleAdapter workScheduleAdapter, WorkScheduleEntityMapper workScheduleEntityMapper, IdGenerationService idGenerationService,
                                   UserAdapter userAdapter, CacheKeyUtil cacheKeyUtil, @Nullable RedisTemplate<String, Object> redisTemplate, CacheLoaderService cacheLoaderService, ApplicationEventPublisher publisher, CacheKeyConfig cacheKeyConfig, CacheReloadHandlerRegistry cacheReloadHandlerRegistry) {
        this.workScheduleAdapter = workScheduleAdapter;
        this.workScheduleEntityMapper = workScheduleEntityMapper;
        this.idGenerationService = idGenerationService;
        this.userAdapter = userAdapter;
        this.cacheKeyUtil = cacheKeyUtil;
        this.redisTemplate = redisTemplate;
        this.cacheLoaderService = cacheLoaderService;
        this.publisher = publisher;
        this.cacheKeyConfig = cacheKeyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
    }

    @Override
    public List<WorkScheduleDto> getAllWorkSchedules(String orgId) {
        String redisKey = cacheKeyUtil.getWorkSchedule(orgId);
        ObjectMapper mapper = new ObjectMapper();

        try {
            if (redisTemplate != null) {
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

                if (entries != null && !entries.isEmpty()) {
                    log.info("Cache hit for orgId {}", orgId);

                    List<WorkScheduleDto> cachedSchedules = new ArrayList<>();
                    for (Object value : entries.values()) {
                        WorkScheduleDto dto = mapper.readValue(value.toString(), WorkScheduleDto.class);
                        cachedSchedules.add(dto);
                    }

                    return cachedSchedules;
                }
            } else {
                log.warn("RedisTemplate is null, skipping cache fetch for orgId {}", orgId);
            }

            // Cache miss, load from DB and repopulate cache
            log.info("Cache miss for workSchedule, loading from DB...");
            Map<String, List<WorkScheduleDto>> loadedMap = cacheLoaderService.loadWorkSchedule(orgId).get();
            List<WorkScheduleDto> response = loadedMap.get(orgId);

            if (response != null && !response.isEmpty()) {
                log.info("Loaded {} workSchedules from DB for orgId {}", response.size(), orgId);

                // Deduplicate by scheduleId if needed
                Map<String, WorkScheduleDto> uniqueMap = response.stream()
                        .collect(Collectors.toMap(
                                WorkScheduleDto::getScheduleId,
                                dto -> dto,
                                (existing, replacement) -> existing
                        ));

                return new ArrayList<>(uniqueMap.values());
            } else {
                log.warn("No workSchedules found in DB for orgId {}", orgId);
                return List.of();
            }

        } catch (Exception e) {
            log.error("Error fetching work schedules for orgId {}: {}", orgId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public ApiResponse createWorkSchedule(WorkSchedule model, String orgId) {
        if (workScheduleAdapter.findByWorkschedule(model.getScheduleName(), orgId)) {
            return new ApiResponse(403, "WorkSchedule Name already exists", false);
        }

        if (model.isDefault()) {
            workScheduleAdapter.resetDefaultWorkSchedule(orgId);
        }

        WorkScheduleTypeEntity typeEntity = workScheduleAdapter.findById(model.getType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid WorkScheduleType ID: " + model.getType()));

        OrganizationEntity organizationEntity = userAdapter.findByOrgId(orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Organization not found"));

        WorkScheduleEntity entity = workScheduleEntityMapper.toEntity(model);
        entity.setOrganizationEntity(organizationEntity);
        entity.setScheduleId(idGenerationService.generateNextId(IdGenerationType.WORK_SCHEDULE));
        entity.setType(typeEntity);
        entity.setActive(true);

        boolean shouldAssignToSuperAdmin = model.isDefault() || workScheduleAdapter.countByOrgId(orgId) == 0;

        WorkScheduleEntity savedEntity = workScheduleAdapter.saveWorkSchedule(entity);

        if (shouldAssignToSuperAdmin) {
            int superAdminRoleLevel = UserRole.SUPERADMIN.getHierarchyLevel();
            List<UserEntity> superAdminUser = userAdapter.findUserByOrgIdAndRoleId(orgId, superAdminRoleLevel);
            List<UserEntity> userEntities = new ArrayList<>();
            if (superAdminUser != null) {
                for (UserEntity user : superAdminUser) {
                    UserEntity userEntity = new UserEntity();
                    userEntity.setWorkSchedule(savedEntity);
                    userEntities.add(userEntity);
                }
                userAdapter.save(userEntities);
            }
        }
        switch (typeEntity.getType()) {
            case FIXED -> saveFixedSchedule(model.getFixedSchedule(), entity);
            case FLEXIBLE -> saveFlexibleSchedule(model.getFlexibleSchedule(), entity);
            case WEEKLY -> saveWeeklySchedule(model.getWeeklySchedule(), entity);
        }

        if (!isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getWorkSchedule(),
                    orgId,
                    cacheReloadHandlerRegistry
            );
            log.info("WorkScheduleCacheReloadEvent published after WorkSchedule Added");
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache WS create reload.");
        }

        return new ApiResponse(201, "WorkSchedule Created Successfully", null);
    }

    private void saveWeeklySchedule(WeeklyScheduleDto dto, WorkScheduleEntity parent) {
        WeeklyWorkScheduleEntity entity = workScheduleEntityMapper.toWeeklyEntity(dto);
        entity.setWeeklyWorkScheduleId(idGenerationService.generateNextId(IdGenerationType.WEEKLY_WORK));
        entity.setWorkScheduleEntity(parent);
        workScheduleAdapter.save(entity);
    }

    private void saveFlexibleSchedule(List<FlexibleScheduleDto> flexibleSchedule, WorkScheduleEntity parent) {
        List<FlexibleWorkScheduleEntity> entities = workScheduleEntityMapper.toFlexibleEntity(flexibleSchedule);
        List<java.lang.String> ids = idGenerationService.generateNextId(IdGenerationType.FLEXIBLE_WORK, entities.size());

        for (int i = 0; i < entities.size(); i++) {
            FlexibleWorkScheduleEntity entity = entities.get(i);
            entity.setFlexibleWorkScheduleId(ids.get(i));
            entity.setWorkScheduleEntity(parent);
        }
        workScheduleAdapter.saveAllFlexible(entities);
    }

    private void saveFixedSchedule(List<FixedScheduleDto> dtos, WorkScheduleEntity parent) {
        List<FixedWorkScheduleEntity> entities = workScheduleEntityMapper.toEntity(dtos);

        List<String> ids = idGenerationService.generateNextId(IdGenerationType.FIXED_WORK, entities.size());

        for (int i = 0; i < entities.size(); i++) {
            FixedWorkScheduleEntity entity = entities.get(i);
            entity.setFixedWorkScheduleId(ids.get(i));
            entity.setWorkScheduleEntity(parent);
        }
        workScheduleAdapter.saveAll(entities);
    }

    @Override
    public ApiResponse addType(WorkScheduleTypeDto type) {
        WorkScheduleTypeEntity entity = new WorkScheduleTypeEntity();
        java.lang.String generatedId = idGenerationService.generateNextId(IdGenerationType.WORK_SCHEDULE_TYPE);
        entity.setTypeId(generatedId);
        entity.setType(WorkScheduleTypeEnum.valueOf(type.getType()));
        workScheduleAdapter.addType(entity);
        return new ApiResponse(201,"Work Schedule Type saved successfully", true);
    }

    @Override
    @Transactional
    public void updateWorkSchedule(WorkSchedule model, String orgId) {

        if ((workScheduleAdapter.findByScheduleName(model.getScheduleId(), model.getScheduleName(), orgId)))
        {
            throw new DataIntegrityViolationException("WorkScheduleName already exists in this organization");
        }
        WorkScheduleEntity existing = workScheduleAdapter.findByScheduleId(model.getScheduleId(), orgId);

            OrganizationEntity organizationEntity = userAdapter.findByOrgId(orgId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Organization not found"));

            WorkScheduleTypeEntity typeEntity = workScheduleAdapter.findById(model.getType())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type"));

            if (model.isDefault()){
                workScheduleAdapter.updateDefaultWorkSchedule(orgId, model.getScheduleId());
            }
            workScheduleAdapter.deleteAllChildren(model.getScheduleId());
            entityManager.flush();
            entityManager.clear();
            WorkScheduleEntity entity = workScheduleEntityMapper.toEntity(model);
            entity.setOrganizationEntity(organizationEntity);
            entity.setType(typeEntity);
            entity.setScheduleId(model.getScheduleId());
            entity.setActive(true);
            boolean shouldAssignToSuperAdmin = model.isDefault() || workScheduleAdapter.countByOrgId(orgId) == 0;
            WorkScheduleEntity savedEntity = workScheduleAdapter.saveWorkSchedule(entity);
            if (shouldAssignToSuperAdmin) {
                int superAdminRoleLevel = UserRole.SUPERADMIN.getHierarchyLevel();
                List<UserEntity> superAdminUser = userAdapter.findUserByOrgIdAndRoleId(orgId, superAdminRoleLevel);
                List<UserEntity> userEntities = new ArrayList<>();
                if (superAdminUser != null) {
                    for (UserEntity user : superAdminUser) {
                        UserEntity userEntity = new UserEntity();
                        userEntity.setWorkSchedule(savedEntity);
                        userEntities.add(userEntity);
                    }
                    userAdapter.save(userEntities);
                }
            }
            switch (typeEntity.getType()) {
                case FIXED -> saveFixedSchedule(model.getFixedSchedule(), entity);
                case FLEXIBLE -> saveFlexibleSchedule(model.getFlexibleSchedule(), entity);
                case WEEKLY -> saveWeeklySchedule(model.getWeeklySchedule(), entity);
            }
        if (!isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getWorkSchedule(),
                    orgId,
                    cacheReloadHandlerRegistry
            );
            log.info("WorkScheduleCacheReloadEvent published after WorkSchedule Updated");
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache WS update reload .");
        }
    }

    @Override
    @Transactional
    public void deleteWorkSchedule(String orgId, String scheduleId) {
        WorkScheduleEntity workSchedule = workScheduleAdapter.findByScheduleId(scheduleId, orgId);

        // 1. Validate organization ownership
        if (!workSchedule.getOrganizationEntity().getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized : Invalid Organization");
        }

        // 2. Prevent deletion of default work schedule
        if (Boolean.TRUE.equals(workSchedule.getDefault())) {
            throw new IllegalStateException("Default Work Schedule cannot be deactivated");
        }

        // 3. Get the default work schedule for this org
        WorkScheduleEntity defaultSchedule = workScheduleAdapter.findDefaultScheduleByOrgId(orgId);
        if (defaultSchedule == null) {
            throw new IllegalStateException("No default Work Schedule found for organization");
        }

        // 4. Update users having the current schedule to the default one
        userAdapter.updateUserWorkSchedule(scheduleId, defaultSchedule.getScheduleId());
        userAdapter.updateGroupWorkSchedule(scheduleId, defaultSchedule.getScheduleId());
        // 5. Mark current schedule as inactive
        workSchedule.setActive(false);
        workScheduleAdapter.saveWorkSchedule(workSchedule);

        if (!isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getWorkSchedule(),
                    orgId,
                    cacheReloadHandlerRegistry
            );
            log.info("WorkSchedule deleted and references updated. Cache reloaded.");
        }
        else {
                log.info("Redis is not enabled or RedisTemplate is null. Skipping cache WS delete reload.");
            }
    }

    @Override
    public List<WorkScheduleType> getAllTypes() {
        List<WorkScheduleType> scheduleTypes = workScheduleAdapter.findAllType().stream()
                .map(workScheduleEntityMapper::toModel)
                .toList();
        return scheduleTypes;
    }
}
