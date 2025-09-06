package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class FaceRegisterResponseDto {

    private int statusCode;
    private String message;
    private List<UserEmbeddingDto> data;

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

    public List<UserEmbeddingDto> getData() {
        return data;
    }

    public void setData(List<UserEmbeddingDto> data) {
        this.data = data;
    }
}
