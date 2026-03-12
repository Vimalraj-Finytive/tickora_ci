package com.uniq.tms.tms_microservice.modules.organizationManagement.scheduler;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationCacheService;
import com.uniq.tms.tms_microservice.modules.userManagement.scheduler.UserRedisScheduler;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.util.List;

@Component
public class OrganizationRedisScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserRedisScheduler.class);

    private final OrganizationRepository organizationRepository;
    private final OrganizationCacheService organizationCacheService;

    public OrganizationRedisScheduler(OrganizationRepository organizationRepository, OrganizationCacheService organizationCacheService) {
        this.organizationRepository = organizationRepository;
        this.organizationCacheService = organizationCacheService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadPrivilegeCacheHourly() {
        log.info("Scheduled Privilege cache loading triggered : {}", LocalTime.now());
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                organizationCacheService.loadPrivilegesFromDB(orgId.getSchemaName());
            }
            log.info("Cache Privilege loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled Privilege cache loading: {}", e.getMessage(), e);
        }finally {
            TenantUtil.clearTenant();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadRolesCacheHourly() {
        log.info("Scheduled Role cache loading triggered : {}", LocalTime.now());
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                organizationCacheService.loadAllRolesToCache(orgId.getOrganizationId(),orgId.getSchemaName());
            }
            log.info("Cache Role loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled Role cache loading: {}", e.getMessage(), e);
        }finally {
            TenantUtil.clearTenant();
        }
    }

}
