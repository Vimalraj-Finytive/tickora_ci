package com.uniq.tms.tms_microservice.modules.userManagement.dto;

public class BulkWorkScheduleUpdateResponseDto {
    private String memberId;
    private boolean success;
    private String message;

    public BulkWorkScheduleUpdateResponseDto(String memberId, boolean success, String message) {
        this.memberId = memberId;
        this.success = success;
        this.message = message;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
