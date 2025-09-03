package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class UserEmbeddingDto {

    private String userId;
    private boolean isRegistered;
    private List<Double> embeddingLink;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public List<Double> getEmbeddingLink() {
        return embeddingLink;
    }

    public void setEmbeddingLink(List<Double> embeddingLink) {
        this.embeddingLink = embeddingLink;
    }
}
