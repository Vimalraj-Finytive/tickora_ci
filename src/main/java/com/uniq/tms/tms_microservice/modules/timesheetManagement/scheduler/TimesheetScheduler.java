package com.uniq.tms.tms_microservice.modules.timesheetManagement.scheduler;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import liquibase.database.core.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TimesheetScheduler {

    private static final Logger log = LoggerFactory.getLogger(TimesheetScheduler.class);

    private final TimesheetService timesheetService;
    private final OrganizationRepository organizationRepository;

    public TimesheetScheduler(TimesheetService timesheetService, OrganizationRepository organizationRepository) {
        this.timesheetService = timesheetService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Kolkata")
    public void autoClockOutForAllEmployees() {
        try{
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for(OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                log.info("Scheduled clock triggered for schema:{}", orgId.getSchemaName());
                String organizationId = orgId.getOrganizationId();
                if(organizationRepository.tableExists(orgId.getSchemaName(),"timesheet")) {
                    log.info("Table exists for schema : {}", orgId.getSchemaName());
                    timesheetService.autoClockOut(organizationId);
                }
                TenantUtil.clearTenant();
            }
        } catch (Exception e) {
            log.error("Error while auto clock out", e);
        }
    }
}
