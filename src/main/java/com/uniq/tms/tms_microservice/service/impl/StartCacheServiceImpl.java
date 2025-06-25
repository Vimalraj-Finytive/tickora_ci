package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.StartCacheService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartCacheServiceImpl implements ApplicationRunner, StartCacheService {

    private final CacheLoaderService cacheLoaderService;

    public StartCacheServiceImpl(CacheLoaderService cacheLoaderService) {
        this.cacheLoaderService = cacheLoaderService;
    }

    //called on application startup
    @Override
    public void run(ApplicationArguments args) throws Exception {
        cacheLoaderService.loadUserTable();
        cacheLoaderService.loadLocationTable();
        cacheLoaderService.loadAllRolesToCache();
        cacheLoaderService.loadPrivilegesFromDB();
    }
}
