package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.StartCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class StartCacheServiceImpl implements ApplicationRunner, StartCacheService {

    private final CacheLoaderService cacheLoaderService;
    private final OrganizationRepository organizationRepository;

    public StartCacheServiceImpl(CacheLoaderService cacheLoaderService, OrganizationRepository organizationRepository) {
        this.cacheLoaderService = cacheLoaderService;
        this.organizationRepository = organizationRepository;
    }

    @Value("${cache.redis.enabled:false}")
    private boolean isRedisEnabled;

    //called on application startup
    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (!isRedisEnabled) {
            System.out.println("Redis cache disabled, skipping cache initialization.");
            return;
        }

        cacheLoaderService.loadAllRolesToCache();
        cacheLoaderService.loadPrivilegesFromDB();
        List<String> orgIds = organizationRepository.findAllOrgIds();
        for(String orgId : orgIds){
            cacheLoaderService.loadLocationTable(orgId);
            cacheLoaderService.loadUsersProfile(orgId);
            cacheLoaderService.loadAllUsers(orgId);
            cacheLoaderService.loadGroupsCache(orgId);
            cacheLoaderService.loadWorkSchedule(orgId);
            cacheLoaderService.loadAllInactiveUsers(orgId);
        }
    }
}
