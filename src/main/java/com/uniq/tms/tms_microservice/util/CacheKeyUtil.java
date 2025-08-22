package com.uniq.tms.tms_microservice.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
