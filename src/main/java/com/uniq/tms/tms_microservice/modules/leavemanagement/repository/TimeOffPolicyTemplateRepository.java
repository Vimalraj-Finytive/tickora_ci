package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeOffPolicyTemplateRepository extends JpaRepository<TimeOffPolicyTemplateEntity, Long> {
    TimeOffPolicyTemplateEntity findByPolicyCode(String policyCode);
}
