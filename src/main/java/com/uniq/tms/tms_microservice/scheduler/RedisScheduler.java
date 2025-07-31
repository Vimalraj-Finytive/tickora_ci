package com.uniq.tms.tms_microservice.scheduler;

import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.util.List;

@Component
public class RedisScheduler {

    private static final Logger log = LoggerFactory.getLogger(RedisScheduler.class);

    private final CacheLoaderService cacheLoaderService;
    private final OrganizationRepository organizationRepository;

    public RedisScheduler(CacheLoaderService cacheLoaderService, OrganizationRepository organizationRepository) {
        this.cacheLoaderService = cacheLoaderService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadLocationCacheHourly() {
        log.info("Scheduled Location cache loading triggered : {}", LocalTime.now());
        try {
            List<String> orgIds = organizationRepository.findAllOrgIds();
            for(String orgId : orgIds){
                cacheLoaderService.loadLocationTable(orgId);
            }
            log.info("Location Cache loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled Location cache loading: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadUserCacheHourly() {
        log.info("Scheduled User cache loading triggered : {}", LocalTime.now());
        try {
            List<String> orgIds = organizationRepository.findAllOrgIds();
            for(String orgId : orgIds){
                cacheLoaderService.loadAllUsers(orgId);
            }
            log.info("Cache User loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled User cache loading: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadProfileCacheHourly() {
        log.info("Scheduled User Profile cache loading triggered : {}", LocalTime.now());
        try {
            List<String> orgIds = organizationRepository.findAllOrgIds();
            for(String orgId : orgIds){
                cacheLoaderService.loadUsersProfile(orgId);
            }
            log.info("Cache User Profile loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled User Profile cache loading: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadRolesCacheHourly() {
        log.info("Scheduled Role cache loading triggered : {}", LocalTime.now());
        try {
            List<String> orgIds = organizationRepository.findAllOrgIds();
            for(String orgId : orgIds) {
                cacheLoaderService.loadGroupsCache(orgId);
            }
            log.info("Cache Role loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled Role cache loading: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadPrivilegeCacheHourly() {
        log.info("Scheduled Privilege cache loading triggered : {}", LocalTime.now());
        try {
                cacheLoaderService.loadPrivilegesFromDB();
            log.info("Cache Privilege loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled Privilege cache loading: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadGroupsCacheHourly() {
        log.info("Scheduled Groups cache loading triggered : {}", LocalTime.now());
        try {
            List<String> orgIds = organizationRepository.findAllOrgIds();
            for(String orgId : orgIds) {
                cacheLoaderService.loadGroupsCache(orgId);
            }
            log.info("Cache Groups loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled Groups cache loading: {}", e.getMessage(), e);
        }
    }
    @Scheduled(cron = "0 0 * * * *")
    public void reloadWorkScheduleCacheHourly() {
        log.info("Scheduled WorkSchedule cache loading triggered : {}", LocalTime.now());
        try {
            List<String> orgIds = organizationRepository.findAllOrgIds();
            for(String orgId : orgIds) {
                cacheLoaderService.loadWorkSchedule(orgId);
            }
            log.info("Cache WorkSchedule loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled WorkSchedule cache loading: {}", e.getMessage(), e);
        }
    }

}
