package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

public class ExportStatus {
    private String status;
    private String filename;

    public ExportStatus(String values, String initialFileName) {
        this.status = values;
        this.filename = initialFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
