package com.uniq.tms.tms_microservice.shared.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyTemplateEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class TimeOffPolicyTemplateLoaderUtil {

    private List<TimeOffPolicyTemplateEntity> templates;

    @PostConstruct
    public void loadTemplates() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();

            InputStream is =
                    getClass().getResourceAsStream("/data/timeoff-policy-templates.json");

            templates = mapper.readValue(
                    is,
                    new TypeReference<List<TimeOffPolicyTemplateEntity>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to load timeoff policy templates", e);
        }
    }

    public List<TimeOffPolicyTemplateEntity> getTemplates() {
        return templates;
    }
}
