package com.uniq.tms.tms_microservice.scheduler;

import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.impl.CacheLoaderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalTime;

@Component
public class RedisScheduler {

    private static final Logger log = LoggerFactory.getLogger(RedisScheduler.class);

    private final CacheLoaderService cacheLoaderService;

    public RedisScheduler(CacheLoaderService cacheLoaderService) {
        this.cacheLoaderService = cacheLoaderService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reloadCacheHourly() {
        log.info("Scheduled cache loading triggered", LocalTime.now());
        try {
            cacheLoaderService.loadLocationTable();
            log.info("Cache loading completed");
        } catch (Exception e) {
            log.error("Error during scheduled cache loading: {}", e.getMessage(), e);
        }
    }

}
