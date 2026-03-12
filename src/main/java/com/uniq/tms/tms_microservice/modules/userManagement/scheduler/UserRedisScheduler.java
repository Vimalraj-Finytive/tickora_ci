package com.uniq.tms.tms_microservice.modules.userManagement.scheduler;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.util.List;

@Component
public class UserRedisScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserRedisScheduler.class);

    private final UserCacheService userCacheService;
    private final OrganizationRepository organizationRepository;

    public UserRedisScheduler(UserCacheService userCacheService, OrganizationRepository organizationRepository) {
        this.userCacheService = userCacheService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadUserCacheHourly() {
        log.info("Scheduled User cache loading triggered : {}", LocalTime.now());
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds){
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                userCacheService.loadAllUsers(orgId.getOrganizationId(),orgId.getSchemaName());
            }
            log.info("Cache User loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled User cache loading: {}", e.getMessage(), e);
        }finally {
            TenantUtil.clearTenant();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadProfileCacheHourly() {
        log.info("Scheduled User Profile cache loading triggered : {}", LocalTime.now());
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds){
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                userCacheService.loadUsersProfile(orgId.getOrganizationId(),orgId.getSchemaName());
            }
            log.info("Cache User Profile loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled User Profile cache loading: {}", e.getMessage(), e);
        }finally {
            TenantUtil.clearTenant();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadGroupsCacheHourly() {
        log.info("Scheduled Groups cache loading triggered : {}", LocalTime.now());
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                userCacheService.loadGroupsCache(orgId.getOrganizationId(),orgId.getSchemaName());
                TenantUtil.clearTenant();
            }
            log.info("Cache Groups loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled Groups cache loading: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadInactiveUserCacheHourly() {
        log.info("Scheduled Inactive User cache loading triggered : {}", LocalTime.now());
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds){
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                userCacheService.loadAllInactiveUsers(orgId.getOrganizationId(),orgId.getSchemaName());
                TenantUtil.clearTenant();
            }
            log.info("Cache Inactive User loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled User cache loading: {}", e.getMessage(), e);
        }
    }
}
