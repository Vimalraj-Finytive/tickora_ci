package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyTemplateEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyTemplateModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TimeOffPolicyTemplatesAdapter {
    long count();
    List<TimeOffPolicyTemplateEntity> findAll();
    void saveAll(List<TimeOffPolicyTemplateEntity> templates);
    void save(TimeOffPolicyTemplateEntity template);
    TimeOffPolicyTemplateEntity findByTemplateCode(String policyCode);
}
