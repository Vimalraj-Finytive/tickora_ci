package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class FaceDto {

    private MultipartFile faceImage;
    private String timesheetLogsJson;
    @JsonIgnore
    private List<TimesheetHistoryDto> timesheetLogs;

    public MultipartFile getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(MultipartFile faceImage) {
        this.faceImage = faceImage;
    }

    public String getTimesheetLogsJson() {
        return timesheetLogsJson;
    }

    public void setTimesheetLogsJson(String timesheetLogsJson) {
        this.timesheetLogsJson = timesheetLogsJson;
    }

    public List<TimesheetHistoryDto> getTimesheetLogs() {
        return timesheetLogs;
    }

    public void setTimesheetLogs(List<TimesheetHistoryDto> timesheetLogs) {
        this.timesheetLogs = timesheetLogs;
    }
}
