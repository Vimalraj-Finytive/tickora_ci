package com.uniq.tms.tms_microservice.modules.locationManagement.scheduler;

import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationCacheService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.util.List;

@Component
public class LocationRedisScheduler {

    private static final Logger log = LoggerFactory.getLogger(LocationRedisScheduler.class);

    private final LocationCacheService locationCacheService;
    private final OrganizationRepository organizationRepository;

    public LocationRedisScheduler(LocationCacheService locationCacheService, OrganizationRepository organizationRepository) {
        this.locationCacheService = locationCacheService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadLocationCacheHourly() {
        log.info("Scheduled Location cache loading triggered : {}", LocalTime.now());
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds){
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                locationCacheService.loadLocationTable(orgId.getOrganizationId(),orgId.getSchemaName());
            }
            log.info("Location Cache loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled Location cache loading: {}", e.getMessage(), e);
        } finally {
            TenantUtil.clearTenant();
        }
    }
}
