package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class AddMemberDto {

    private Long groupId;
    private List<Long> groupMember;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<Long> getGroupMember() {
        return groupMember;
    }

    public void setGroupMember(List<Long> groupMember) {
        this.groupMember = groupMember;
    }
}
