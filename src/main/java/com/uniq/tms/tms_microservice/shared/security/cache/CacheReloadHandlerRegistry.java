package com.uniq.tms.tms_microservice.shared.security.cache;

import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationCacheService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationCacheService;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserCacheService;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.services.WorkScheduleCacheService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class CacheReloadHandlerRegistry {

    private final Map<String, BiConsumer<String,String>> reloadHandlers = new HashMap<>();

        public CacheReloadHandlerRegistry(WorkScheduleCacheService workScheduleCacheLoaderService,
                                          UserCacheService userCacheService, LocationCacheService locationCacheService,
                                          OrganizationCacheService organizationCacheService,
                                          CacheKeyConfig cacheKeyConfig) {
            reloadHandlers.put(cacheKeyConfig.getLocation(), locationCacheService::loadLocationTable);
            reloadHandlers.put(cacheKeyConfig.getUsers(), userCacheService::loadAllUsers);
            reloadHandlers.put(cacheKeyConfig.getGroups(), userCacheService::loadGroupsCache);
            reloadHandlers.put(cacheKeyConfig.getUserprofile(), userCacheService::loadUsersProfile);
            reloadHandlers.put(cacheKeyConfig.getRoleprivilege(), organizationCacheService::loadAllRolesToCache);
            reloadHandlers.put(cacheKeyConfig.getRoleprivilege(), (orgId,schema) -> organizationCacheService.loadPrivilegesFromDB(schema));
            reloadHandlers.put(cacheKeyConfig.getWorkSchedule(), workScheduleCacheLoaderService::loadWorkSchedule);
            reloadHandlers.put(cacheKeyConfig.getInactiveUsers(), userCacheService::loadAllInactiveUsers);
        }

        public void reload(String cacheName, String orgId, String schema) {
            BiConsumer<String,String> handler = reloadHandlers.get(cacheName);
            if (handler != null) {
                handler.accept(orgId,schema);
            }
        }
}
