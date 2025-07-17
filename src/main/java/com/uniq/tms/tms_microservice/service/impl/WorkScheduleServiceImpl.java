package com.uniq.tms.tms_microservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.config.CacheKeyConfig;
import com.uniq.tms.tms_microservice.config.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.enums.IdGenerationType;
import com.uniq.tms.tms_microservice.enums.WorkScheduleTypeEnum;
import com.uniq.tms.tms_microservice.mapper.WorkScheduleEntityMapper;
import com.uniq.tms.tms_microservice.model.WorkSchedule;
import com.uniq.tms.tms_microservice.model.WorkScheduleType;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.IdGeneratorService;
import com.uniq.tms.tms_microservice.service.WorkScheduleService;
import com.uniq.tms.tms_microservice.util.CacheEventPublisherUtil;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final WorkScheduleAdapter workScheduleAdapter;
    private final WorkScheduleEntityMapper workScheduleEntityMapper;
    private final IdGeneratorService idGeneratorService;
    private final UserAdapter userAdapter;
    private final CacheKeyUtil cacheKeyUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheLoaderService cacheLoaderService;
    private final ApplicationEventPublisher publisher;
    private final CacheKeyConfig cacheKeyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;

    public WorkScheduleServiceImpl(WorkScheduleAdapter workScheduleAdapter, WorkScheduleEntityMapper workScheduleEntityMapper, IdGeneratorService idGeneratorService, UserAdapter userAdapter, CacheKeyUtil cacheKeyUtil, @Nullable RedisTemplate<String, Object> redisTemplate, CacheLoaderService cacheLoaderService, ApplicationEventPublisher publisher, CacheKeyConfig cacheKeyConfig, CacheReloadHandlerRegistry cacheReloadHandlerRegistry) {
        this.workScheduleAdapter = workScheduleAdapter;
        this.workScheduleEntityMapper = workScheduleEntityMapper;
        this.idGeneratorService = idGeneratorService;
        this.userAdapter = userAdapter;
        this.cacheKeyUtil = cacheKeyUtil;
        this.redisTemplate = redisTemplate;
        this.cacheLoaderService = cacheLoaderService;
        this.publisher = publisher;
        this.cacheKeyConfig = cacheKeyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
    }

    @Override
    public List<WorkScheduleDto> getAllWorkSchedules(Long orgId) {
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
            Map<Long, List<WorkScheduleDto>> loadedMap = cacheLoaderService.loadWorkSchedule(orgId).get();
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
    public ApiResponse createWorkSchedule(WorkSchedule model, com.uniq.tms.tms_microservice.dto.WorkScheduleDto dto, Long orgId) {

        boolean exist = workScheduleAdapter.findByWorkschedule(dto.getScheduleName(), orgId);
        if (exist) {
            return new ApiResponse(403, "WorkSchedule Name already Exist", false);
        }

        if(dto.isDefault()){
            workScheduleAdapter.resetDefaultWorkSchedule(orgId);
        }
        WorkScheduleTypeEntity typeEntity = workScheduleAdapter.findById(dto.getType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid work schedule type ID: " + dto.getType()));

        OrganizationEntity organizationEntity = userAdapter.findByOrgId(orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Organization not found"));

        WorkScheduleEntity entity = workScheduleEntityMapper.toEntity(model);
        entity.setOrganizationEntity(organizationEntity);
        entity.setScheduleId(idGeneratorService.generateNextId(IdGenerationType.WORK_SCHEDULE));
        entity.setType(typeEntity);
        entity.setActive(true);
        workScheduleAdapter.saveWorkSchedule(entity);

        switch (typeEntity.getType()) {
            case FIXED -> saveFixedSchedule(dto.getFixedSchedule(), entity);
            case FLEXIBLE -> saveFlexibleSchedule(dto.getFlexibleSchedule(), entity);
            case WEEKLY -> saveWeeklySchedule(dto.getWeeklySchedule(), entity);
        }
        CacheEventPublisherUtil.syncReloadThenPublish(
                publisher,
                cacheKeyConfig.getWorkSchedule(),
                orgId,
                cacheReloadHandlerRegistry
        );
        log.info("WorkScheduleCacheReloadEvent published after WorkSchedule Added");

        return new ApiResponse(201, "WorkSchedule Created Successfully", null);
    }

    private void saveWeeklySchedule(WeeklyScheduleDto dto, WorkScheduleEntity parent) {
        WeeklyWorkScheduleEntity entity = workScheduleEntityMapper.toWeeklyEntity(dto);
        entity.setWeeklyWorkScheduleId(idGeneratorService.generateNextId(IdGenerationType.WEEKLY_WORK));
        entity.setWorkScheduleEntity(parent);
        workScheduleAdapter.save(entity);
    }

    private void saveFlexibleSchedule(List<FlexibleScheduleDto> flexibleSchedule, WorkScheduleEntity parent) {
        List<FlexibleWorkScheduleEntity> entities = workScheduleEntityMapper.toFlexibleEntity(flexibleSchedule);
        List<java.lang.String> ids = idGeneratorService.generateNextId(IdGenerationType.FLEXIBLE_WORK, entities.size());

        for (int i = 0; i < entities.size(); i++) {
            FlexibleWorkScheduleEntity entity = entities.get(i);
            entity.setFlexibleWorkScheduleId(ids.get(i));
            entity.setWorkScheduleEntity(parent);
        }
        workScheduleAdapter.saveAllFlexible(entities);
    }

    private void saveFixedSchedule(List<FixedScheduleDto> dtos, WorkScheduleEntity parent) {
        List<FixedWorkScheduleEntity> entities = workScheduleEntityMapper.toEntity(dtos);

        List<String> ids = idGeneratorService.generateNextId(IdGenerationType.FIXED_WORK, entities.size());

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
        java.lang.String generatedId = idGeneratorService.generateNextId(IdGenerationType.WORK_SCHEDULE_TYPE);
        entity.setTypeId(generatedId);
        entity.setType(WorkScheduleTypeEnum.valueOf(type.getType()));
        workScheduleAdapter.addType(entity);
        return new ApiResponse(201,"Work Schedule Type saved successfully", true);
    }

    @Override
    @Transactional
    public void updateWorkSchedule(WorkSchedule model, WorkScheduleDto dto, Long orgId) {

        if ((workScheduleAdapter.findByScheduleName(dto.getScheduleId(), dto.getScheduleName(), orgId)))
        {
            throw new DataIntegrityViolationException("WorkScheduleName already exists in this organization");
        }
        // Validate
        WorkScheduleEntity existing = workScheduleAdapter.findByScheduleId(dto.getScheduleId());

            OrganizationEntity organizationEntity = userAdapter.findByOrgId(orgId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Organization not found"));

            WorkScheduleTypeEntity typeEntity = workScheduleAdapter.findById(dto.getType())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type"));
            if (dto.isDefault()){
                workScheduleAdapter.updateDefaultWorkSchedule(orgId, dto.getScheduleId());
            }
            // Delete children
            workScheduleAdapter.deleteAllChildren(dto.getScheduleId());
            entityManager.flush();
            entityManager.clear();
            // Update parent
            WorkScheduleEntity entity = workScheduleEntityMapper.toEntity(model);
            entity.setOrganizationEntity(organizationEntity);
            entity.setType(typeEntity);
            entity.setScheduleId(dto.getScheduleId());
            entity.setActive(true);
            workScheduleAdapter.saveWorkSchedule(entity);
            switch (typeEntity.getType()) {
                case FIXED -> saveFixedSchedule(dto.getFixedSchedule(), entity);
                case FLEXIBLE -> saveFlexibleSchedule(dto.getFlexibleSchedule(), entity);
                case WEEKLY -> saveWeeklySchedule(dto.getWeeklySchedule(), entity);
            }
        CacheEventPublisherUtil.syncReloadThenPublish(
                publisher,
                cacheKeyConfig.getWorkSchedule(),
                orgId,
                cacheReloadHandlerRegistry
        );
        log.info("WorkScheduleCacheReloadEvent published after WorkSchedule Updated");
        }

    @Override
    @Transactional
    public void deleteWorkSchedule(Long orgId, String scheduleId) {
        WorkScheduleEntity workSchedule = workScheduleAdapter.findByScheduleId(scheduleId);

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

        // Cache reload
        CacheEventPublisherUtil.syncReloadThenPublish(
                publisher,
                cacheKeyConfig.getWorkSchedule(),
                orgId,
                cacheReloadHandlerRegistry
        );

        log.info("WorkSchedule deleted and references updated. Cache reloaded.");
    }

    @Override
    public List<WorkScheduleType> getAllTypes() {
        List<WorkScheduleType> scheduleTypes = workScheduleAdapter.findAllType().stream()
                .map(workScheduleEntityMapper::toModel)
                .toList();
        return scheduleTypes;
    }
}
