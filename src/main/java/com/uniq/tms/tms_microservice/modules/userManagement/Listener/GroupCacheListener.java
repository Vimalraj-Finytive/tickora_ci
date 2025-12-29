package com.uniq.tms.tms_microservice.modules.userManagement.Listener;

import com.uniq.tms.tms_microservice.modules.userManagement.event.GroupEvent;
import com.uniq.tms.tms_microservice.shared.helper.CacheReloadHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class GroupCacheListener {

    private static final Logger log = LogManager.getLogger(GroupCacheListener.class);

    private final CacheReloadHelper cacheReloadHelper;

    public GroupCacheListener(CacheReloadHelper cacheReloadHelper) {
        this.cacheReloadHelper = cacheReloadHelper;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGroupEvent(GroupEvent event){
        log.info("Event listener reached for group");
        cacheReloadHelper.refreshGroupCache(event.orgId(), event.schema());
    }
}
