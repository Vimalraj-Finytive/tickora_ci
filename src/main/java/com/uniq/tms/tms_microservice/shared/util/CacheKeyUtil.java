package com.uniq.tms.tms_microservice.shared.util;

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
    @Value("${cache.keys.otpCount}")
    private String otpCount;
    @Value("${cache.keys.otpKey}")
    private String otpKey;
    @Value("${cache.keys.payment.usedOrder}")
    private String paymentUsedOrder;

    private static final Logger log = LogManager.getLogger(CacheKeyUtil.class);

    public String getLocationKey(String orgId, String schema){
        return location + ":" + schema + ":" + orgId;
    }

    public String getProfileKey(String orgId, String schema){
        return userProfile + ":" + schema + ":" + orgId;
    }

    public String getMemberKey(String orgId, String schema){
        return users + ":" + schema + ":" + orgId;
    }

    public String getAllGroupsKey(String orgId, String schema){ return groups + ":" + schema + ":" + orgId; }

    public String getSupervisedGroupsKey(String orgId, String schema){ return groups+ ":supervised:" + schema + ":" + orgId;}

    public String getRoleKey(String schema) {
        return schema + ":"+ roleprivilege;
    }

    public String getWorkSchedule(String orgId, String schema){
        return workSchedule + ":" + schema + ":" + orgId;
    }

    public String getInactiveMemberKey(String orgId, String schema){
        return inactiveUsers + ":" +  schema + ":" + orgId;
    }

    public String getOtpCountKey(String orgId, String schema, String userId) {
        return otpCount + ":" + schema + ":" + orgId + ":" + userId;
    }

    public String getOtpKey(String orgId, String schema, String mobile) {
        return otpKey + ":" + schema + ":" + orgId + ":" + mobile;
    }

    public String getPaymentUsedOrderKey(String schema,String orgId,String orderId) {
        return paymentUsedOrder + ":" + schema + ":" + orgId + ":" + orderId;
    }
}
