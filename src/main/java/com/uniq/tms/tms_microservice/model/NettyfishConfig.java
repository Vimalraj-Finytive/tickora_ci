package com.uniq.tms.tms_microservice.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "nettyfish")
public class NettyfishConfig {

    private String senderId;
    private String templateId;
    private String principleEntityId;
    private String apiKey;
    private String clientId;
    private boolean unicode;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getPrincipleEntityId() {
        return principleEntityId;
    }

    public void setPrincipleEntityId(String principleEntityId) {
        this.principleEntityId = principleEntityId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean unicode() {
        return unicode;
    }

    public void setUnicode(boolean unicode) {
        unicode = unicode;
    }
}
