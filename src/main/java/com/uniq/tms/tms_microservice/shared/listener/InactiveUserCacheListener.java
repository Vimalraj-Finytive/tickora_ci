package com.uniq.tms.tms_microservice.shared.listener;

import com.uniq.tms.tms_microservice.shared.event.InactiveUserEvent;
import com.uniq.tms.tms_microservice.shared.event.UserEvent;
import com.uniq.tms.tms_microservice.shared.helper.CacheReloadHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InactiveUserCacheListener {

    private static final Logger log = LogManager.getLogger(InactiveUserCacheListener.class);

    private final CacheReloadHelper cacheReloadHelper;

    public InactiveUserCacheListener(CacheReloadHelper cacheReloadHelper) {
        this.cacheReloadHelper = cacheReloadHelper;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInactiveUserEvent(InactiveUserEvent event){
        log.info("Inactive User Event listener reached");
        log.info("listener thread: {}", Thread.currentThread().getName());
        cacheReloadHelper.refreshInactiveUserCache(event.orgId(), event.schema());
    }
}
