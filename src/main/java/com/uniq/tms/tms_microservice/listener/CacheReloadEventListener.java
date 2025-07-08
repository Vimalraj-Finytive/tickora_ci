package com.uniq.tms.tms_microservice.listener;

import com.uniq.tms.tms_microservice.config.CacheDependencyConfig;
import com.uniq.tms.tms_microservice.config.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.event.*;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CacheReloadEventListener {

    private static final Logger log = LoggerFactory.getLogger(CacheReloadEventListener.class);

    private final CacheLoaderService cacheLoaderService;
    private final CacheDependencyConfig cacheDependencyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;

    public CacheReloadEventListener(CacheLoaderService cacheLoaderService, CacheDependencyConfig cacheDependencyConfig, CacheReloadHandlerRegistry cacheReloadHandlerRegistry) {
        this.cacheLoaderService = cacheLoaderService;
        this.cacheDependencyConfig = cacheDependencyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
    }

    @EventListener
    public void CacheReload(CacheReloadEvent event) {
        log.info("Received CacheReloadEvent. Reloading cache...");
        String cacheName = event.getCacheName();
        Long orgId = event.getOrgId();

        List<String> dependents = cacheDependencyConfig.getDependent(cacheName);
        log.info("Dependents of cachename: {} are : {}", cacheName, dependents);
        for (String dependent : dependents) {
            log.info("Reloading dependent cache: {}", dependent);
            cacheReloadHandlerRegistry.reload(dependent, orgId);
        }    }
}
