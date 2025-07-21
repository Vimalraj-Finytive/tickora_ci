package com.uniq.tms.tms_microservice.util;

import com.uniq.tms.tms_microservice.config.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.event.CacheReloadEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

public class CacheEventPublisherUtil {

    private static final Logger log = LogManager.getLogger(CacheEventPublisherUtil.class);

    public static void syncReloadThenPublish(ApplicationEventPublisher publisher,
                                             String cacheName,
                                             String orgId,
                                             CacheReloadHandlerRegistry registry) {
        log.info("Reloading primary cache synchronously: {}", cacheName);
        registry.reload(cacheName, orgId);

        log.info("Publishing CacheReloadEvent for dependents of {}...", cacheName);
        publisher.publishEvent(new CacheReloadEvent(cacheName, orgId));
    }
}
