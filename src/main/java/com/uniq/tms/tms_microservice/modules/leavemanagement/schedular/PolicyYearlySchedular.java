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
public class PolicyYearlySchedular {

    private static final Logger log = LoggerFactory.getLogger(PolicyYearlySchedular.class);

    private final TimeOffPolicyService timeOffPolicyService;
    private final OrganizationRepository organizationRepository;

    public PolicyYearlySchedular(TimeOffPolicyService timeOffPolicyService, OrganizationRepository organizationRepository) {
        this.timeOffPolicyService = timeOffPolicyService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0/2 * * * ?", zone = "Asia/Kolkata")
    public void autoUpdateYearlyPolicy(){
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for (OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                try {
                    log.info("Scheduled clock triggered for update yearly policy");
                    timeOffPolicyService.updateYearlyPolicy();
                } catch (Exception e) {
                    continue;
                } finally {
                    TenantUtil.clearTenant();
                }
            }
        } catch (Exception e) {
            log.error("Error while yearlyPolicy update", e);
        }
    }
}
