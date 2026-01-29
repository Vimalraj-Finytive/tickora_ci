package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.shared.security.cache.CacheKeyConfig;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.shared.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.shared.util.CacheEventPublisherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CacheReloadHelper {

    private static final Logger log = LoggerFactory.getLogger(CacheReloadHelper.class);

    private final ApplicationEventPublisher publisher;
    private final CacheKeyConfig cacheKeyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;

    public CacheReloadHelper(ApplicationEventPublisher publisher, CacheKeyConfig cacheKeyConfig,
                             CacheReloadHandlerRegistry cacheReloadHandlerRegistry) {
        this.publisher = publisher;
        this.cacheKeyConfig = cacheKeyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
    }

    @Value("${cache.redis.enabled}")
    private boolean isRedisEnabled;

    public void refreshUserCache(String orgId, String schema) {
        if (!isRedisEnabled) {
            log.info("Redis disabled. Skipping user cache refresh for orgId={}", orgId);
            return;
        }
        try {
            TenantContext.setCurrentTenant(schema);
            log.info("reload helper");
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getUsers(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("User cache refreshed for orgId={}", orgId);
        } catch (Exception e) {
            log.error(
                    "Failed to refresh user cache for orgId={}",
                    orgId,
                    e
            );
        } finally {
            TenantContext.clear();
        }
    }

    public void refreshGroupCache(String orgId, String schema) {
        if (!isRedisEnabled) {
            log.info("Redis disabled. Skipping Group cache refresh for orgId={}", orgId);
            return;
        }
        try {
            TenantContext.setCurrentTenant(schema);
            log.info("reload Group helper");
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getGroups(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("Group cache refreshed for orgId={}", orgId);
        } catch (Exception e) {
            log.error(
                    "Failed to refresh Group cache for orgId={}",
                    orgId,
                    e
            );
        } finally {
            TenantContext.clear();
        }
    }

    public void refreshInactiveUserCache(String orgId, String schema) {
        if (!isRedisEnabled) {
            log.info("Redis disabled. Skipping Inactive User cache refresh for orgId={}", orgId);
            return;
        }
        try {
            TenantContext.setCurrentTenant(schema);
            log.info("reload Inactive user helper");
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getInactiveUsers(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("Inactive User cache refreshed for orgId={}", orgId);
        } catch (Exception e) {
            log.error(
                    "Failed to refresh Inactive User cache for orgId={}",
                    orgId,
                    e
            );
        } finally {
            TenantContext.clear();
        }
    }

    public void refreshUserProfileCache(String orgId, String schema) {
        if (!isRedisEnabled) {
            log.info("Redis disabled. Skipping User Profile cache refresh for orgId={}", orgId);
            return;
        }
        try {
            TenantContext.setCurrentTenant(schema);
            log.info("reload User Profile helper");
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getUserprofile(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("User Profile cache refreshed for orgId={}", orgId);
        } catch (Exception e) {
            log.error(
                    "Failed to refresh User Profile cache for orgId={}",
                    orgId,
                    e
            );
        } finally {
            TenantContext.clear();
        }
    }

    public void refreshWorkScheduleCache(String orgId, String schema) {
        if (!isRedisEnabled) {
            log.info("Redis disabled. Skipping WorkSchedule cache refresh for orgId={}", orgId);
            return;
        }
        try {
            TenantContext.setCurrentTenant(schema);
            log.info("reload WorkSchedule helper");
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getWorkSchedule(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("WorkSchedule cache refreshed for orgId={}", orgId);
        } catch (Exception e) {
            log.error(
                    "Failed to refresh WorkSchedule cache for orgId={}",
                    orgId,
                    e
            );
        } finally {
            TenantContext.clear();
        }
    }

    public void refreshLocationCache(String orgId, String schema) {
        if (!isRedisEnabled) {
            log.info("Redis disabled. Skipping Location cache refresh for orgId={}", orgId);
            return;
        }
        try {
            TenantContext.setCurrentTenant(schema);
            log.info("reload Location helper");
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getLocation(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("Location cache refreshed for orgId={}", orgId);
        } catch (Exception e) {
            log.error(
                    "Failed to refresh Location cache for orgId={}",
                    orgId,
                    e
            );
        } finally {
            TenantContext.clear();
        }
    }

}
