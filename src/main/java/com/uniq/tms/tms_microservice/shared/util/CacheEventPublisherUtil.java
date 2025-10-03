package com.uniq.tms.tms_microservice.shared.util;

import com.uniq.tms.tms_microservice.shared.security.cache.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.shared.event.CacheReloadEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

public class CacheEventPublisherUtil {

    private static final Logger log = LogManager.getLogger(CacheEventPublisherUtil.class);

    public static void syncReloadThenPublish(ApplicationEventPublisher publisher,
                                             String cacheName,
                                             String orgId,
                                             String schema,
                                             CacheReloadHandlerRegistry registry) {
        log.info("Reloading primary cache synchronously: {} : {}", cacheName, schema);
        registry.reload(cacheName, orgId, schema);

        log.info("Publishing CacheReloadEvent for dependents of {}...{}", cacheName, schema);
        publisher.publishEvent(new CacheReloadEvent(cacheName, orgId, schema));
    }
}
