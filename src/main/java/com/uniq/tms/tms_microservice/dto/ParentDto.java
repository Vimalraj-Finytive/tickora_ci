package com.uniq.tms.tms_microservice.dto;

public class ParentDto{

    private String parentId;
    private String parentName;
    private String email;
    private String mobileNumber;

    public ParentDto(String id, String userName, String email, String mobile) {
        this.parentId = id;
        this.parentName = userName;
        this.email = email;
        this.mobileNumber = mobile;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
