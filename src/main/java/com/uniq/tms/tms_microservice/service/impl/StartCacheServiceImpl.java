package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.StartCacheService;
import com.uniq.tms.tms_microservice.util.TenantUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class StartCacheServiceImpl implements ApplicationRunner, StartCacheService {

    private final CacheLoaderService cacheLoaderService;
    private final OrganizationRepository organizationRepository;

    public StartCacheServiceImpl(CacheLoaderService cacheLoaderService, OrganizationRepository organizationRepository) {
        this.cacheLoaderService = cacheLoaderService;
        this.organizationRepository = organizationRepository;
    }

    @Value("${cache.redis.enabled:false}")
    private boolean isRedisEnabled;

    @Override
    public void run(ApplicationArguments args) {

        if (!isRedisEnabled) {
            log.info("Redis cache disabled, skipping cache initialization.");
            return;
        }

        log.info("Starting Cache Initialization for All Organizations");

        List<OrganizationEntity> organizations = organizationRepository.findAll();
        for (OrganizationEntity org : organizations) {
            String orgSchema = org.getSchemaName();
            String orgId = org.getOrganizationId();

            try {
                TenantUtil.setCurrentTenant(orgSchema);
                log.info("Starting cache initialization for tenant: {}", orgSchema);

                cacheLoaderService.loadLocationTable(orgId, orgSchema);
                cacheLoaderService.loadUsersProfile(orgId, orgSchema);
                cacheLoaderService.loadAllUsers(orgId, orgSchema);
                cacheLoaderService.loadGroupsCache(orgId, orgSchema);
                cacheLoaderService.loadWorkSchedule(orgId, orgSchema);
                cacheLoaderService.loadAllInactiveUsers(orgId, orgSchema);
                cacheLoaderService.loadAllRolesToCache(orgId, orgSchema);
                cacheLoaderService.loadPrivilegesFromDB(orgSchema);

                log.info("Cache initialization completed for tenant: {}", orgSchema);
            } catch (Exception e) {
                log.error("Cache initialization failed for tenant: {} | Reason: {}", orgSchema, e.getMessage(), e);
            } finally {
                TenantUtil.clearTenant();
            }
        }
        log.info("All Cache Initialization Tasks Completed");
    }
}
