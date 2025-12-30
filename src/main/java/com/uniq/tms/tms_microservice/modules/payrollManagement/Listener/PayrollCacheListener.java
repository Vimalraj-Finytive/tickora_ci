package com.uniq.tms.tms_microservice.modules.payrollManagement.Listener;

import com.uniq.tms.tms_microservice.modules.payrollManagement.event.PayrollCreatedEvent;
import com.uniq.tms.tms_microservice.shared.helper.CacheReloadHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PayrollCacheListener {

    private static final Logger log = LogManager.getLogger(PayrollCacheListener.class);

    private final CacheReloadHelper cacheReloadHelper;

    public PayrollCacheListener(CacheReloadHelper cacheReloadHelper) {
        this.cacheReloadHelper = cacheReloadHelper;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPayrollCreated(PayrollCreatedEvent event){
        log.info("Event listener reached");
        log.info("listener thread: {}", Thread.currentThread().getName());
        cacheReloadHelper.refreshUserCache(event.orgId(), event.schema());
    }
}
