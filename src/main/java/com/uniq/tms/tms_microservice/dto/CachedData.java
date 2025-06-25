package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.List;
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class CachedData<T> {

    private List<T> data;
    private LocalDateTime cachedAt;

    public CachedData() {}

    public CachedData(List<T> data, LocalDateTime cachedAt) {
        this.data = data;
        this.cachedAt = cachedAt;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }
}
