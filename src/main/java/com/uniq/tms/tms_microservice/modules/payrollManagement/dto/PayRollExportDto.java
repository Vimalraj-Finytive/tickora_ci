package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

public class PayRollExportDto {

    private String month;
    private  String format;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
