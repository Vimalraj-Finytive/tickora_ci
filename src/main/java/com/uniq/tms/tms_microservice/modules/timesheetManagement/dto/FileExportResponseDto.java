package com.uniq.tms.tms_microservice.modules.timesheetManagement.dto;

import java.nio.file.Path;

public class FileExportResponseDto {

    private Path filePath;
    private String fileName;
    private String format;

    public FileExportResponseDto(Path filePath, String fileName, String format) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.format = format;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
