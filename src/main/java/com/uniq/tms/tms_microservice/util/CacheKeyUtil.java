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
    @Value("${cache.keys.inactiveUsers}")
    private String inactiveUsers;

    private static final Logger log = LogManager.getLogger(CacheKeyUtil.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleRepository roleRepository;

    public CacheKeyUtil(@Nullable RedisTemplate<String, Object> redisTemplate, RoleRepository roleRepository) {
        this.redisTemplate = redisTemplate;
        this.roleRepository = roleRepository;
    }

    public boolean roleHasPrivilege(String roleName, String privilegeKey) {
        if (privilegeKey == null || privilegeKey.trim().isEmpty()) {
            log.warn("Privilege key is null or empty. Skipping privilege check.");
            return false;
        }

        String redisKey = roleprivilege + roleName.toLowerCase();
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

        privileges = roleOpt.get().getPrivilegeEntities().stream()
                .map(PrivilegeEntity::getName)
                .collect(Collectors.toSet());

        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(redisKey, privileges);
            log.info("Repopulated Redis cache for role '{}'", roleName);
        }

        log.info("Privilege for key:{}", privileges.contains(privilegeKey));
        return privileges.contains(privilegeKey);
    }

    public String getLocationKey(String orgId){
        return location +":orgId:" + orgId;
    }

    public String getprofileKey(String orgId){
        return userProfile +":orgId:" + orgId;
    }

    public String getMemberKey(String orgId){
        return users +":orgId:" + orgId;
    }

    public String getAllGroupsKey(String orgId){ return groups +":orgId:" + orgId; }

    public String getSupervisedGroupsKey(String orgId){ return groups+ "supervised:" +":orgId:" + orgId;}

    public String getRoleKey() {
        return roleprivilege;
    }

    public String getWorkSchedule(String orgId){
        return workSchedule +":orgId:" + orgId;
    }

    public String getInactiveMemberKey(String orgId){
        return inactiveUsers +":orgId:" + orgId;
    }

    public String getOtpCountKey(String orgId, String userId) {
        return "otpCount:" + orgId + ":" + userId;
    }

    public String getOtpKey(String orgId, String mobile) {
        return "otp:" + orgId + ":" + mobile;
    }
}
