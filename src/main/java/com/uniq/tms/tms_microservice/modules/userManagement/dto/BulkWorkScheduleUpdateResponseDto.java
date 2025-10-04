package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BulkWorkScheduleUpdateResponseDto {
    private String memberId;
    private boolean success;
    private String message;
}
