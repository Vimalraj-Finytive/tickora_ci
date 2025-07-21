package com.uniq.tms.tms_microservice.config;

import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class CacheReloadHandlerRegistry {

    private final Map<String, Consumer<String>> reloadHandlers = new HashMap<>();

        public CacheReloadHandlerRegistry(CacheLoaderService cacheLoaderService, CacheKeyConfig cacheKeyConfig) {
            reloadHandlers.put(cacheKeyConfig.getLocation(), cacheLoaderService::loadLocationTable);
            reloadHandlers.put(cacheKeyConfig.getUsers(), cacheLoaderService::loadAllUsers);
            reloadHandlers.put(cacheKeyConfig.getGroups(), cacheLoaderService::loadGroupsCache);
            reloadHandlers.put(cacheKeyConfig.getUserprofile(), cacheLoaderService::loadUsersProfile);
            reloadHandlers.put(cacheKeyConfig.getRoleprivilege(), orgId -> cacheLoaderService.loadAllRolesToCache());
            reloadHandlers.put(cacheKeyConfig.getRoleprivilege(), orgId -> cacheLoaderService.loadPrivilegesFromDB());
            reloadHandlers.put(cacheKeyConfig.getWorkSchedule(), cacheLoaderService::loadWorkSchedule);
        }

        public void reload(String cacheName, String orgId) {
            Consumer<String> handler = reloadHandlers.get(cacheName);
            if (handler != null) {
                handler.accept(orgId);
            }
        }
}
