package com.uniq.tms.tms_microservice.modules.leavemanagement.schedular;

import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffRequestService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaveManagementSchedular {

    private static final Logger log = LoggerFactory.getLogger(LeaveManagementSchedular.class);

    private final TimeOffRequestService timeOffRequestService;
    private final OrganizationRepository organizationRepository;

    public LeaveManagementSchedular(TimeOffRequestService timeOffRequestService, OrganizationRepository organizationRepository) {
        this.timeOffRequestService = timeOffRequestService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void autoUpdateLeaveBalance(){
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for (OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                try {
                    log.info("Scheduled clock triggered for calculate Leave Balance");
                    timeOffRequestService.updateLeaveBalance();
                } catch (Exception e) {
                    continue;
                } finally {
                    TenantUtil.clearTenant();
                }
            }
        } catch (Exception e) {
            log.error("Error while leaveBalance calculate", e);
        }
    }
}
