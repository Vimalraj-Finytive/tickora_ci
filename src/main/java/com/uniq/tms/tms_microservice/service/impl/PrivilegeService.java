package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PrivilegeService {

    private final UserAdapter useradapter;

    public PrivilegeService(UserAdapter useradapter) {
        this.useradapter = useradapter;
    }

    @Cacheable("rolePrivilegeMap")
    public Map<String, Set<String>> getRolePrivilegeMap() {
        List<RoleEntity> roles = useradapter.findAllWithPrivileges();
        Map<String, Set<String>> map = new HashMap<>();

        for (RoleEntity role : roles) {
            Set<String> privileges = role.getPrivilegeEntities()
                    .stream()
                    .map(PrivilegeEntity::getName)
                    .collect(Collectors.toSet());
            map.put(role.getName(), privileges);
        }

        System.out.println("RolePrivilegeMap: " + map);
        return map;
    }
}
