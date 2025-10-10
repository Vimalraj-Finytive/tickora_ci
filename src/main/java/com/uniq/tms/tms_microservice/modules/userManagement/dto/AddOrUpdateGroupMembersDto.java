package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.util.List;

public class AddOrUpdateGroupMembersDto {

    private List<Long> groupIds;
    private List<String> userIds;
    private String type;

    public List<Long> getGroupIds() { return groupIds; }
    public void setGroupIds(List<Long> groupIds) { this.groupIds = groupIds; }

    public List<String> getUserIds() { return userIds; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
