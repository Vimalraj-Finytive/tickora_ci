package com.uniq.tms.tms_microservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class CacheKeyUtil {

    @Value("${CACHE_ROLE_PRIVILEGE}")
    private String roleprivilege;

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheKeyUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean roleHasPrivilege(String roleName, String privilegeKey) {
        Set<String> privileges = (Set<String>) redisTemplate.opsForValue().get(roleprivilege + roleName.toLowerCase());
        return privileges != null && privileges.contains(privilegeKey);
    }

}
