package com.uniq.tms.tms_microservice.helper;

import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.util.TenantUtil;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RolePrivilegeHelper {

    private static final Logger log = LogManager.getLogger(RolePrivilegeHelper .class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleRepository roleRepository;
    private final CacheKeyUtil cacheKeyUtil;

    @Value("${cache.keys.roleprivilege}")
    private String roleprivilege;

    public RolePrivilegeHelper(@Nullable RedisTemplate<String, Object> redisTemplate, RoleRepository roleRepository, CacheKeyUtil cacheKeyUtil) {
        this.redisTemplate = redisTemplate;
        this.roleRepository = roleRepository;
        this.cacheKeyUtil = cacheKeyUtil;
    }

    public boolean roleHasPrivilege(String roleName, String privilegeKey) {
        if (privilegeKey == null || privilegeKey.trim().isEmpty()) {
            log.warn("Privilege key is null or empty. Skipping privilege check.");
            return false;
        }
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current tenant :{}", schema);
        String redisKey = cacheKeyUtil.getRoleKey(schema);
        log.info("Redis key : {}", redisKey);
        Set<String> privileges = null;

        if (redisTemplate != null) {
            privileges = (Set<String>) redisTemplate.opsForValue().get(redisKey);
        }

        if (privileges != null) {
            return privileges.contains(privilegeKey);
        }

        log.warn("Privilege cache miss for role '{}'. Falling back to DB.", roleName);
        Optional<RoleEntity> roleOpt = roleRepository.findByNameWithPrivileges(roleName);
        if (roleOpt.isEmpty()) {
            return false;
        }

        privileges = roleOpt.get().getPrivilegeMappings().stream()
                .map(mapping -> mapping.getPrivilege().getName())
                .collect(Collectors.toSet());

        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(redisKey + roleName.toLowerCase(), privileges);
            log.info("Repopulated Redis cache for role '{}'", roleName);
        }

        log.info("Privilege for key:{}", privileges.contains(privilegeKey));
        return privileges.contains(privilegeKey);
    }
}
