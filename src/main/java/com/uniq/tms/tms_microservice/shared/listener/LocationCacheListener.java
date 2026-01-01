package com.uniq.tms.tms_microservice.shared.listener;

import com.uniq.tms.tms_microservice.shared.event.LocationEvent;
import com.uniq.tms.tms_microservice.shared.event.UserEvent;
import com.uniq.tms.tms_microservice.shared.helper.CacheReloadHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LocationCacheListener {


    private static final Logger log = LogManager.getLogger(LocationCacheListener.class);

    private final CacheReloadHelper cacheReloadHelper;

    public LocationCacheListener(CacheReloadHelper cacheReloadHelper) {
        this.cacheReloadHelper = cacheReloadHelper;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLocationEvent(LocationEvent event){
        log.info("Event listener reached");
        log.info("listener thread: {}", Thread.currentThread().getName());
        cacheReloadHelper.refreshLocationCache(event.orgId(), event.schema());
    }

}
