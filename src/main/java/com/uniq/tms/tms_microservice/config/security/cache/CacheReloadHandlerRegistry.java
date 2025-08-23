package com.uniq.tms.tms_microservice.config.security.cache;

import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class CacheReloadHandlerRegistry {

    private final Map<String, BiConsumer<String,String>> reloadHandlers = new HashMap<>();

        public CacheReloadHandlerRegistry(CacheLoaderService cacheLoaderService, CacheKeyConfig cacheKeyConfig) {
            reloadHandlers.put(cacheKeyConfig.getLocation(), cacheLoaderService::loadLocationTable);
            reloadHandlers.put(cacheKeyConfig.getUsers(), cacheLoaderService::loadAllUsers);
            reloadHandlers.put(cacheKeyConfig.getGroups(), cacheLoaderService::loadGroupsCache);
            reloadHandlers.put(cacheKeyConfig.getUserprofile(), cacheLoaderService::loadUsersProfile);
            reloadHandlers.put(cacheKeyConfig.getRoleprivilege(), cacheLoaderService::loadAllRolesToCache);
            reloadHandlers.put(cacheKeyConfig.getRoleprivilege(), (orgId,schema) -> cacheLoaderService.loadPrivilegesFromDB(schema));
            reloadHandlers.put(cacheKeyConfig.getWorkSchedule(), cacheLoaderService::loadWorkSchedule);
            reloadHandlers.put(cacheKeyConfig.getInactiveUsers(), cacheLoaderService::loadAllInactiveUsers);
        }

        public void reload(String cacheName, String orgId, String schema) {
            BiConsumer<String,String> handler = reloadHandlers.get(cacheName);
            if (handler != null) {
                handler.accept(orgId,schema);
            }
        }
}
