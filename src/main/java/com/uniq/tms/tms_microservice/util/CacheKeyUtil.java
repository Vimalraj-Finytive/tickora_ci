package com.uniq.tms.tms_microservice.util;

import com.uniq.tms.tms_microservice.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
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
public class CacheKeyUtil {

    @Value("${cache.keys.roleprivilege}")
    private String roleprivilege;
    @Value("${cache.keys.location}")
    private String location;
    @Value("${cache.keys.userprofile}")
    private String userProfile;
    @Value("${cache.keys.users}")
    private String users;
    @Value("${cache.keys.groups}")
    private String groups;
    @Value("${cache.keys.workSchedule}")
    private String workSchedule;

    private static final Logger log = LogManager.getLogger(CacheKeyUtil.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleRepository roleRepository;

    public CacheKeyUtil(@Nullable RedisTemplate<String, Object> redisTemplate, RoleRepository roleRepository) {
        this.redisTemplate = redisTemplate;
        this.roleRepository = roleRepository;
    }

    public boolean roleHasPrivilege(String roleName, String privilegeKey) {
        String redisKey = roleprivilege + roleName.toLowerCase();

        Set<String> privileges = null;
        if (redisTemplate != null) {
            privileges = (Set<String>) redisTemplate.opsForValue().get(redisKey);
        }

        // Cache hit
        if (privileges != null) {
            return privileges.contains(privilegeKey);
        }

        // Cache miss - fallback to DB
        log.warn("Privilege cache miss for role '{}'. Falling back to DB.", roleName);
        Optional<RoleEntity> roleOpt = roleRepository.findByNameWithPrivileges(roleName);
        if (roleOpt.isEmpty()) {
            return false;
        }

        privileges = roleOpt.get().getPrivilegeEntities().stream()
                .map(PrivilegeEntity::getName)
                .collect(Collectors.toSet());

        // Optional: repopulate Redis
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(redisKey, privileges);
            log.info("Repopulated Redis cache for role '{}'", roleName);
        }

        return privileges.contains(privilegeKey);
    }

    public String getLocationKey(Long orgId){
        return location +":orgId:" + orgId;
    }

    public String getprofileKey(Long orgId){
        return userProfile +":orgId:" + orgId;
    }

    public String getMemberKey(Long orgId){
        return users +":orgId:" + orgId;
    }

    public String getAllGroupsKey(Long orgId){ return groups +":orgId:" + orgId; }

    public String getSupervisedGroupsKey(Long orgId){ return groups+ "supervised:" +":orgId:" + orgId;}

    public String getRoleKey() {
        return roleprivilege;
    }

    public String getWorkSchedule(Long orgId){
        return workSchedule +":orgId:" + orgId;
    }
}
