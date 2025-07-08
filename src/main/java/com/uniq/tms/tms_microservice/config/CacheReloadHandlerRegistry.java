package com.uniq.tms.tms_microservice.config;

import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class CacheReloadHandlerRegistry {

    private final Map<String, Consumer<Long>> reloadHandlers = new HashMap<>();

        public CacheReloadHandlerRegistry(CacheLoaderService cacheLoaderService, CacheKeyConfig cacheKeyConfig) {
            reloadHandlers.put(cacheKeyConfig.getLocation(), cacheLoaderService::loadLocationTable);
            reloadHandlers.put(cacheKeyConfig.getUsers(), cacheLoaderService::loadAllUsers);
            reloadHandlers.put(cacheKeyConfig.getGroups(), cacheLoaderService::loadGroupsCache);
            reloadHandlers.put(cacheKeyConfig.getUserprofile(), cacheLoaderService::loadUsersProfile);
            reloadHandlers.put(cacheKeyConfig.getRoleprivilege(), cacheLoaderService::loadAllRolesToCache);
            reloadHandlers.put(cacheKeyConfig.getRoleprivilege(), cacheLoaderService::loadPrivilegesFromDB);
        }

        public void reload(String cacheName, Long orgId) {
            Consumer<Long> handler = reloadHandlers.get(cacheName);
            if (handler != null) {
                handler.accept(orgId);
            }
        }
}
