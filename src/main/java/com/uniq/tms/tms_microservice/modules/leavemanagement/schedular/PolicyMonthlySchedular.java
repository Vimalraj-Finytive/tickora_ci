package com.uniq.tms.tms_microservice.modules.leavemanagement.schedular;

import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffPolicyService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PolicyMonthlySchedular {
    private static final Logger log = LoggerFactory.getLogger(PolicyMonthlySchedular.class);

    private final TimeOffPolicyService timeOffPolicyService;
    private final OrganizationRepository organizationRepository;

    public PolicyMonthlySchedular(TimeOffPolicyService timeOffPolicyService, OrganizationRepository organizationRepository) {
        this.timeOffPolicyService = timeOffPolicyService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 0 1 * ?", zone = "Asia/Kolkata")
    public void autoUpdateMonthlyPolicy(){
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for (OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                try {
                    log.info("Scheduled clock triggered for update monthly policy");
                    timeOffPolicyService.updateMonthlyPolicy();
                } catch (Exception e) {
                    continue;
                } finally {
                    TenantUtil.clearTenant();
                }
            }
        } catch (Exception e) {
            log.error("Error while monthlyPolicy update", e);
        }
    }

}
