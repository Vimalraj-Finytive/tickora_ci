package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class UserClockResponseDto {

    private int statusCode;
    private String message;
    private List<UserClockStatusDto> data;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<UserClockStatusDto> getData() {
        return data;
    }

    public void setData(List<UserClockStatusDto> data) {
        this.data = data;
    }
}
