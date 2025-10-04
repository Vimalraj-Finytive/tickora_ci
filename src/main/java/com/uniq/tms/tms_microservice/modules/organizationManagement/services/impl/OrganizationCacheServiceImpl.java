package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationCacheService;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.RoleEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.PrivilegeRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.RoleRepository;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class OrganizationCacheServiceImpl implements OrganizationCacheService {

    private final Map<PrivilegeConstants, String> privilegeMap = new ConcurrentHashMap<>();

    private static final Logger log = LogManager.getLogger(OrganizationCacheServiceImpl.class);

    private final CacheKeyUtil cacheKeyUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    public OrganizationCacheServiceImpl(CacheKeyUtil cacheKeyUtil, @Nullable RedisTemplate<String, Object> redisTemplate, RoleRepository roleRepository, PrivilegeRepository privilegeRepository) {
        this.cacheKeyUtil = cacheKeyUtil;
        this.redisTemplate = redisTemplate;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    /**
     *
     * @param
     * @return roles from db and cache it redis cache using keys
     */
    public void loadAllRolesToCache(String orgId, String schema) {

        List<RoleEntity> roles = roleRepository.findAllWithPrivileges();

        Map<String, Set<String>> rolePrivileges = new HashMap<>();
        for (RoleEntity role : roles) {
            Set<String> privileges = role.getPrivilegeMappings().stream()
                    .map(mapping -> mapping.getPrivilege().getName())
                    .collect(Collectors.toSet());
            rolePrivileges.put(role.getName(), privileges);
        }

        String redisKey = cacheKeyUtil.getRoleKey(schema);
        if (redisTemplate != null) {
            rolePrivileges.forEach((role, privileges) -> {
                redisTemplate.opsForValue().set(redisKey + role.toLowerCase(), privileges);
                log.info("Loaded role {} with privileges: {}", role, privileges);
            });
        } else {
            log.warn("RedisTemplate is null, skipping cache for roles.");
        }
    }

    private final Map<String, Map<PrivilegeConstants, String>> schemaPrivilegeMap = new HashMap<>();
    public String getPrivilegeKey(PrivilegeConstants constant) {
        String schema = TenantUtil.getCurrentTenant();
        if (schema == null) {
            log.error("Schema is null in TenantContext! Cannot fetch privilege key for {}", constant);
            return null;
        }
        schemaPrivilegeMap.putIfAbsent(schema, new HashMap<>());
        if (schemaPrivilegeMap.get(schema).isEmpty()){
            loadPrivilegesFromDB(schema);
        }
        return privilegeMap.get(constant);
    }

    public void loadPrivilegesFromDB(String schema) {
        List<PrivilegeEntity> privileges = privilegeRepository.findAll();
        log.info("Loading privileges from DB: {}", privileges.size());

        for (PrivilegeConstants constant : PrivilegeConstants.values()) {
            log.info("Loading privilege from Enum: {}", constant.name());
            privileges.stream()
                    .filter(p -> p.getStaticName().equalsIgnoreCase(constant.name()))
                    .findFirst()
                    .ifPresentOrElse(
                            p -> privilegeMap.put(constant, p.getName()),
                            () -> log.warn("Privilege NOT FOUND in DB for constant: {}", constant.name())
                    );
        }

        schemaPrivilegeMap.put(schema, privilegeMap);
        // Log all mapped privileges
        privilegeMap.forEach((key, value) ->
                log.info("Privilege constant: {}, Privilege name: {}", key, value));
    }

}
