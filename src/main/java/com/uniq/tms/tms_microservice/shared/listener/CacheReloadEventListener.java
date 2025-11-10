package com.uniq.tms.tms_microservice.shared.listener;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.shared.event.CacheReloadEvent;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheDependencyConfig;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheReloadHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class CacheReloadEventListener {

    private static final Logger log = LoggerFactory.getLogger(CacheReloadEventListener.class);

    private final CacheDependencyConfig cacheDependencyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;
    private final Map<String, List<LocationDto>> locationCache;

    public CacheReloadEventListener(CacheDependencyConfig cacheDependencyConfig, CacheReloadHandlerRegistry cacheReloadHandlerRegistry,
                                    Map<String, List<LocationDto>> locationCache) {
        this.cacheDependencyConfig = cacheDependencyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
        this.locationCache = locationCache;
    }

    @EventListener
    public void CacheReload(CacheReloadEvent event) {
        log.info("Received CacheReloadEvent. Reloading cache...");
        String cacheName = event.getCacheName();
        String orgId = event.getOrgId();
        String schema = event.getSchema();
        List<String> dependents = cacheDependencyConfig.getDependent(cacheName);
        log.info("Dependents of cachename: {} are : {}", cacheName, dependents);
        for (String dependent : dependents) {
            log.info("Reloading dependent cache: {}", dependent);
            cacheReloadHandlerRegistry.reload(dependent, orgId,schema);
        }    }

    @EventListener
    public void onCacheReload(CacheReloadEvent event) {
        if ("location".equalsIgnoreCase(event.getCacheName())) {
            locationCache.clear();
            log.info("Cleared local user-location cache after Redis reload");
        }
    }
}
