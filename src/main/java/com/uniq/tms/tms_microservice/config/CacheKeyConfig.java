package com.uniq.tms.tms_microservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cache.keys")
public class CacheKeyConfig {

    private String location;
    private String users;
    private String groups;
    private String userprofile;
    private String roleprivilege;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getUserprofile() {
        return userprofile;
    }

    public void setUserprofile(String userprofile) {
        this.userprofile = userprofile;
    }

    public String getRoleprivilege() {
        return roleprivilege;
    }

    public void setRoleprivilege(String roleprivilege) {
        this.roleprivilege = roleprivilege;
    }
}
