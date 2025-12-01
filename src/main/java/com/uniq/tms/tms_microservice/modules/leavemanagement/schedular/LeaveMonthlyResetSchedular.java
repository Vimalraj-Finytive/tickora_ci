package com.uniq.tms.tms_microservice.modules.leavemanagement.schedular;

import com.uniq.tms.tms_microservice.modules.leavemanagement.services.LeaveBalanceService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LeaveMonthlyResetSchedular {

    private static final Logger log = LoggerFactory.getLogger(LeaveMonthlyResetSchedular.class);

    private final LeaveBalanceService leaveBalanceService;
    private final OrganizationRepository organizationRepository;

    public LeaveMonthlyResetSchedular(LeaveBalanceService leaveBalanceService, OrganizationRepository organizationRepository) {
        this.leaveBalanceService = leaveBalanceService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 0 1 * ?", zone = "Asia/Kolkata")
    public void autoUpdateMonthlyLeaveBalance(){
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for (OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                try {
                    log.info("Scheduled clock triggered for Update LeaveBalance monthly");
                    leaveBalanceService.updateMonthlyLeaveBalance();
                } catch (Exception e) {
                    continue;
                } finally {
                    TenantUtil.clearTenant();
                }
            }
        } catch (Exception e) {
            log.error("Error while leaveBalance monthly update", e);
        }
    }
}
