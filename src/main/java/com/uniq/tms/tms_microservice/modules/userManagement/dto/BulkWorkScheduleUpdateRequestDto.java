package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkWorkScheduleUpdateRequestDto {

    @NotEmpty(message = "Member IDs cannot be empty")
    private List<String> memberIds;

    @NotEmpty(message = "Work Schedule ID cannot be empty")
    private String workScheduleId;

    private String reason;

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public String getWorkScheduleId() {
        return workScheduleId;
    }

    public void setWorkScheduleId(String workScheduleId) {
        this.workScheduleId = workScheduleId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
