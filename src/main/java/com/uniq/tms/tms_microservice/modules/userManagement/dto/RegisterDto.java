package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import org.springframework.web.multipart.MultipartFile;

public class RegisterDto {

    private String userId;
    private MultipartFile faceImage;

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
}
