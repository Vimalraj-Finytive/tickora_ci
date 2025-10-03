package com.uniq.tms.tms_microservice.modules.identityManagement.service.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationCacheService;
import com.uniq.tms.tms_microservice.modules.identityManagement.service.StartCacheService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationCacheService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationService;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserCacheService;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.services.WorkScheduleCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class StartCacheServiceImpl implements ApplicationRunner, StartCacheService {

    private final WorkScheduleCacheService workScheduleCacheLoaderService;
    private final UserCacheService userCacheService;
    private final OrganizationService organizationService;
    private final LocationCacheService locationCacheService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationCacheService organizationCacheService;

    public StartCacheServiceImpl(WorkScheduleCacheService workScheduleCacheLoaderService, UserCacheService userCacheService, OrganizationService organizationService, LocationCacheService locationCacheService, OrganizationRepository organizationRepository, OrganizationCacheService organizationCacheService) {
        this.workScheduleCacheLoaderService = workScheduleCacheLoaderService;
        this.userCacheService = userCacheService;
        this.organizationService = organizationService;
        this.locationCacheService = locationCacheService;
        this.organizationRepository = organizationRepository;
        this.organizationCacheService = organizationCacheService;
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

                locationCacheService.loadLocationTable(orgId, orgSchema);
                userCacheService.loadUsersProfile(orgId, orgSchema);
                userCacheService.loadAllUsers(orgId, orgSchema);
                userCacheService.loadGroupsCache(orgId, orgSchema);
                workScheduleCacheLoaderService.loadWorkSchedule(orgId, orgSchema);
                userCacheService.loadAllInactiveUsers(orgId, orgSchema);
                organizationCacheService.loadAllRolesToCache(orgId, orgSchema);
                organizationCacheService.loadPrivilegesFromDB(orgSchema);

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
