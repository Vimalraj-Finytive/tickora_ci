package com.uniq.tms.tms_microservice.modules.userManagement.model;

import java.util.List;

public class DeleteMemberModel {
    Long groupId;
    List<String> memberId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<String> getMemberId() {
        return memberId;
    }

    public void setMemberId(List<String> memberId) {
        this.memberId = memberId;
    }
}
