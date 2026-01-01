package com.uniq.tms.tms_microservice.shared.listener;

import com.uniq.tms.tms_microservice.shared.event.WorkScheduleEvent;
import com.uniq.tms.tms_microservice.shared.helper.CacheReloadHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WorkSchedulerCacheListener {

    private static final Logger log = LogManager.getLogger(WorkSchedulerCacheListener.class);

    private final CacheReloadHelper cacheReloadHelper;

    public WorkSchedulerCacheListener(CacheReloadHelper cacheReloadHelper) {
        this.cacheReloadHelper = cacheReloadHelper;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkScheduleEvent(WorkScheduleEvent event){
        log.info("Event listener reached");
        log.info("listener thread: {}", Thread.currentThread().getName());
        cacheReloadHelper.refreshWorkScheduleCache(event.orgId(), event.schema());
    }
}
