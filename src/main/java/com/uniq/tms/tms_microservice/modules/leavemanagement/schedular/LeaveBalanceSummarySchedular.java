package com.uniq.tms.tms_microservice.modules.leavemanagement.schedular;

import com.uniq.tms.tms_microservice.modules.leavemanagement.services.LeaveBalanceService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LeaveBalanceSummarySchedular {

    private static final Logger log = LoggerFactory.getLogger(LeaveBalanceSummarySchedular.class);

    private final LeaveBalanceService leaveBalanceService;
    private final OrganizationRepository organizationRepository;
    private final AuthHelper authHelper;

    public LeaveBalanceSummarySchedular(LeaveBalanceService leaveBalanceService, OrganizationRepository organizationRepository, AuthHelper authHelper) {
        this.leaveBalanceService = leaveBalanceService;
        this.organizationRepository = organizationRepository;
        this.authHelper = authHelper;
    }

@Scheduled(cron = "0 0 0 1 * ?", zone = "Asia/Kolkata")
public void autoUpdateLeaveSummary(){
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for (OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                try {
                    log.info("Scheduled clock triggered for calculate Leave Summary");
                    leaveBalanceService.updateMonthlyLeaveSummary(orgId.getOrganizationId());
//                    leaveBalanceService.updateDailyLeaveSummary();
                } catch (Exception e) {
                    continue;
//                      throw new RuntimeException(e);
                } finally {
                    TenantUtil.clearTenant();
                }
            }
        } catch (Exception e) {
            log.error("Error while leaveSummary calculate", e);
        }
    }

}
