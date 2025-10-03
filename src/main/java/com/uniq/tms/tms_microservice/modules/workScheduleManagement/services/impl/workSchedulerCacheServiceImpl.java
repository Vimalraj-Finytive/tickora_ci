package com.uniq.tms.tms_microservice.modules.workScheduleManagement.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.FixedScheduleDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.FlexibleScheduleDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WorkScheduleDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.mapper.WorkScheduleDtoMapper;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.WorkSchedule;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.projection.WorkScheduleData;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.repository.WorkScheduleRepository;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.services.WorkScheduleCacheService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class workSchedulerCacheServiceImpl implements WorkScheduleCacheService {

    private final Map<PrivilegeConstants, String> privilegeMap = new ConcurrentHashMap<>();
    private static final Logger log = LogManager.getLogger(workSchedulerCacheServiceImpl.class);

    private final WorkScheduleDtoMapper workScheduleDtoMapper;
    private final WorkScheduleRepository workScheduleRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheKeyUtil cacheKeyUtil;

    public workSchedulerCacheServiceImpl(WorkScheduleDtoMapper workScheduleDtoMapper, WorkScheduleRepository workScheduleRepository, RedisTemplate<String, Object> redisTemplate, CacheKeyUtil cacheKeyUtil) {
        this.workScheduleDtoMapper = workScheduleDtoMapper;
        this.workScheduleRepository = workScheduleRepository;
        this.redisTemplate = redisTemplate;
        this.cacheKeyUtil = cacheKeyUtil;
    }

    /**
     *
     * @param orgId
     * @return workSchedule based on organization from db and cache it redis cache using keys
     */
    @Transactional
    public CompletableFuture<Map<String, List<WorkScheduleDto>>>
    loadWorkSchedule(String orgId, String schema) {

        log.info("Current WS tenant:{}", schema);
        List<WorkScheduleData> rawSchedules = workScheduleRepository.findAllWithChildrenByOrgId(orgId);
        ObjectMapper mapper = new ObjectMapper();

        List<WorkScheduleDto> workScheduleDtos = new ArrayList<>();

        for (WorkScheduleData raw : rawSchedules) {
            // Convert entity JSON fields into WorkSchedule (intermediate object)
            WorkSchedule schedule = new WorkSchedule();
            schedule.setScheduleId(raw.getWorkScheduleId());
            schedule.setScheduleName(raw.getWorkScheduleName());
            schedule.setDefault(raw.getIsDefault());
            schedule.setDefault(raw.getIsDefault());
            schedule.setActive(raw.getIsActive());
            schedule.setType(raw.getWorkScheduleType());
            schedule.setOrgId(raw.getOrganizationId());
            schedule.setWeeklySchedule(raw.getWeeklySchedule());

            // Parse fixed and flexible schedules from JSON strings
            try {
                if (raw.getFixedSchedule() != null && !raw.getFixedSchedule().isBlank()) {
                    List<FixedScheduleDto> fixed = mapper.readValue(
                            raw.getFixedSchedule(),
                            new TypeReference<List<FixedScheduleDto>>() {
                            }
                    );
                    schedule.setFixedSchedule(fixed);
                }
            } catch (Exception e) {
                log.error("FixedSchedule parse error for ID {}: {}", raw.getWorkScheduleId(), e.getMessage());
                schedule.setFixedSchedule(Collections.emptyList());
            }

            try {
                if (raw.getFlexibleSchedule() != null && !raw.getFlexibleSchedule().isBlank()) {
                    List<FlexibleScheduleDto> flex = mapper.readValue(
                            raw.getFlexibleSchedule(),
                            new TypeReference<List<FlexibleScheduleDto>>() {
                            }
                    );
                    schedule.setFlexibleSchedule(flex);
                }
            } catch (Exception e) {
                log.error("FlexibleSchedule parse error for ID {}: {}", raw.getWorkScheduleId(), e.getMessage());
                schedule.setFlexibleSchedule(Collections.emptyList());
            }

            log.info("Parsed fixedSchedule for {}: {}", raw.getWorkScheduleId(), schedule.getFixedSchedule());

            WorkScheduleDto dto = workScheduleDtoMapper.toDtoWithFormattedTimes(schedule);
            workScheduleDtos.add(dto);
        }

        Map<String, List<WorkScheduleDto>> userWorkScheduleMap = new HashMap<>();
        userWorkScheduleMap.put(orgId, workScheduleDtos);

        // Redis Caching
        String redisKey = cacheKeyUtil.getWorkSchedule(orgId,schema);
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(redisKey);
                Map<String, String> redisMap = new HashMap<>();
                for (WorkScheduleDto dto : workScheduleDtos) {
                    redisMap.put(dto.getScheduleId(), mapper.writeValueAsString(dto));
                }
                redisTemplate.opsForHash().putAll(redisKey, redisMap);
                log.info("Cached {} workSchedules under key: {}", redisMap.size(), redisKey);
            }
        } catch (Exception e) {
            log.warn("Redis caching failed for orgId {}: {}", orgId, e.getMessage());
        }

        return CompletableFuture.completedFuture(userWorkScheduleMap);
    }

}
