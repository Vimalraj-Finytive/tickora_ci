package com.uniq.tms.tms_microservice.modules.timesheetManagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ClockInOutRequestDto {

    private String userId;
    private MultipartFile faceImage;
    private String timesheetLogsJson;
    @JsonIgnore
    private List<TimesheetHistoryDto> timesheetLogs;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MultipartFile getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(MultipartFile faceImage) {
        this.faceImage = faceImage;
    }

    public List<TimesheetHistoryDto> getTimesheetLogs() {
        return timesheetLogs;
    }

    public void setTimesheetLogs(List<TimesheetHistoryDto> timesheetLogs) {
        this.timesheetLogs = timesheetLogs;
    }

    public String getTimesheetLogsJson() {
        return timesheetLogsJson;
    }

    public void setTimesheetLogsJson(String timesheetLogsJson) {
        this.timesheetLogsJson = timesheetLogsJson;
    }
}
