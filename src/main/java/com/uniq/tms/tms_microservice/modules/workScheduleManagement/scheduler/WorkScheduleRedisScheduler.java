package com.uniq.tms.tms_microservice.modules.workScheduleManagement.scheduler;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.services.WorkScheduleCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.util.List;

@Component
public class WorkScheduleRedisScheduler {

    private static final Logger log = LoggerFactory.getLogger(WorkScheduleRedisScheduler.class);

    private final WorkScheduleCacheService workScheduleCacheLoaderService;
    private final OrganizationRepository organizationRepository;

    public WorkScheduleRedisScheduler(WorkScheduleCacheService workScheduleCacheLoaderService, OrganizationRepository organizationRepository) {
        this.workScheduleCacheLoaderService = workScheduleCacheLoaderService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadWorkScheduleCacheHourly() {
        log.info("Scheduled WorkSchedule cache loading triggered : {}", LocalTime.now());
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                workScheduleCacheLoaderService.loadWorkSchedule(orgId.getOrganizationId(),orgId.getSchemaName());
            }
            log.info("Cache WorkSchedule loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled WorkSchedule cache loading: {}", e.getMessage(), e);
        }finally {
            TenantUtil.clearTenant();
        }
    }


}
