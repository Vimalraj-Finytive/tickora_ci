package com.uniq.tms.tms_microservice.modules.payrollManagement.schedular;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.payrollManagement.services.PayRollService;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PayRollSchedular {

    private static final Logger log = LoggerFactory.getLogger(PayRollSchedular.class);

    private final PayRollService payRollService;
    private final OrganizationRepository organizationRepository;

    public PayRollSchedular(PayRollService payRollService, OrganizationRepository organizationRepository) {
        this.payRollService = payRollService;
        this.organizationRepository = organizationRepository;
    }

    @Scheduled(cron = "0 0 2 1 * ?", zone = "Asia/Kolkata")
    public void autoCalculatePayrollAmountForAllEmployees(){
        try {
            List<OrganizationEntity> orgIds = organizationRepository.findAll();
            for (OrganizationEntity orgId : orgIds) {
                TenantUtil.setCurrentTenant(orgId.getSchemaName());
                try {
                    log.info("Running payroll scheduler for tenant: {}", orgId.getSchemaName());
                    payRollService.calculateMonthlyPayrollAmount();
//                    payRollService.calculateDailyPayrollAmount();
                } catch (Exception e) {
                    continue;
                } finally {
                    TenantUtil.clearTenant();
                }
            }
        } catch (Exception e) {
        log.error("Error while payroll calculate", e);
    }
    }
}
