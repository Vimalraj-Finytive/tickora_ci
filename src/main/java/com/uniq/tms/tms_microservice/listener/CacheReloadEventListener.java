package com.uniq.tms.tms_microservice.listener;

import com.uniq.tms.tms_microservice.event.LocationCacheReloadEvent;
import com.uniq.tms.tms_microservice.event.PrivilegeCacheReloadEvent;
import com.uniq.tms.tms_microservice.event.RolePrivilegesCacheReloadEvent;
import com.uniq.tms.tms_microservice.event.UserCacheReloadEvent;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CacheReloadEventListener {

    private static final Logger log = LoggerFactory.getLogger(CacheReloadEventListener.class);

    private final CacheLoaderService cacheLoaderService;

    public CacheReloadEventListener(CacheLoaderService cacheLoaderService) {
        this.cacheLoaderService = cacheLoaderService;
    }

    @EventListener
    public void userCacheReload(UserCacheReloadEvent event) {
        log.info("Received UserCacheReloadEvent. Reloading user cache...");
        cacheLoaderService.loadUserTable();
    }

    @EventListener
    public void locationCacheReload(LocationCacheReloadEvent event) {
        log.info("Received LocationCacheReloadEvent. Reloading location cache...");
        cacheLoaderService.loadLocationTable();
    }

    @EventListener
    public void PrivilegeCacheReload(PrivilegeCacheReloadEvent event){
        log.info("Received PrivilegeCacheReloadEvent. Reloading privilege cache...");
        cacheLoaderService.loadPrivilegesFromDB();
        cacheLoaderService.loadAllRolesToCache();
    }

    @EventListener
    public void RolePrivilegeCacheReload(RolePrivilegesCacheReloadEvent event){
        log.info("Received RolePrivilegesCacheReloadEvent. Reloading privilege cache...");
        cacheLoaderService.loadPrivilegesFromDB();
        cacheLoaderService.loadAllRolesToCache();
    }
}
