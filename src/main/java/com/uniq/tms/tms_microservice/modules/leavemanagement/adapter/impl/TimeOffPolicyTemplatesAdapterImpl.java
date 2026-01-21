package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyTemplatesAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyTemplateEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyTemplateModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.TimeOffPolicyTemplateRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimeOffPolicyTemplatesAdapterImpl implements TimeOffPolicyTemplatesAdapter {

    private final TimeOffPolicyTemplateRepository timeOffPolicyTemplateRepository;

    public TimeOffPolicyTemplatesAdapterImpl(TimeOffPolicyTemplateRepository timeOffPolicyTemplateRepository) {
        this.timeOffPolicyTemplateRepository = timeOffPolicyTemplateRepository;
    }

    @Override
    public long count() {
        return timeOffPolicyTemplateRepository.count();
    }

    @Override
    public List<TimeOffPolicyTemplateEntity> findAll() {
        return timeOffPolicyTemplateRepository.findAll();
    }

    @Override
    public void saveAll(List<TimeOffPolicyTemplateEntity> templates) {
        timeOffPolicyTemplateRepository.saveAll(templates);
    }

    @Override
    public void save(TimeOffPolicyTemplateEntity template) {
        timeOffPolicyTemplateRepository.save(template);
    }

    @Override
    public TimeOffPolicyTemplateEntity findByTemplateCode(String policyCode) {
        return timeOffPolicyTemplateRepository.findByPolicyCode(policyCode);
    }
}
