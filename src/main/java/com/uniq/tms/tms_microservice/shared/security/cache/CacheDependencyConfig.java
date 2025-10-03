package com.uniq.tms.tms_microservice.shared.security.cache;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "cache.dependency")
public class CacheDependencyConfig {

    private static final Logger log = LogManager.getLogger(CacheDependencyConfig.class);

    private String location;
    private String users;
    private String groups;
    private String userprofile;
    private String workSchedule;
    private String inactiveUsers;

    private final CacheKeyConfig cacheKeyConfig;

    private final Map<String , String> dependency = new HashMap<>();

    public CacheDependencyConfig(CacheKeyConfig cacheKeyConfig) {
        this.cacheKeyConfig = cacheKeyConfig;
    }

    @PostConstruct
    public void populateDependencyMap() {
        if (users != null) dependency.put(cacheKeyConfig.getUsers(), users);
        if (groups != null) dependency.put(cacheKeyConfig.getGroups(), groups);
        if (location != null) dependency.put(cacheKeyConfig.getLocation(), location);
        if (userprofile != null) dependency.put(cacheKeyConfig.getUserprofile(), userprofile);
        if (workSchedule != null) dependency.put(cacheKeyConfig.getWorkSchedule(), workSchedule);
        if (inactiveUsers != null) dependency.put(cacheKeyConfig.getInactiveUsers(), inactiveUsers);
    }

    public List<String> getDependent(String cacheName){
        log.info("Dependency Map Loaded: {}", dependency);
        log.info("CacheDependencyConfig - looking for cacheName: {}", cacheName);
        log.info("Current dependency map keys: {}", dependency.keySet());

        String dependent = dependency.getOrDefault(cacheName, "");
        return dependent.isBlank() ? List.of() : Arrays.asList(dependent.split(","));
    }

    public Map<String, String> getDependency(){
        return dependency;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setUserprofile(String userprofile) {
        this.userprofile = userprofile;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }

    public void setInactiveUsers(String inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }
}
