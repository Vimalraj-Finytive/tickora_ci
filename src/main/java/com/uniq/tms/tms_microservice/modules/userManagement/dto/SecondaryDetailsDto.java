package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SecondaryDetailsDto {
    @NotBlank(message = "Secondary User name is required")
    private String userName;
    @NotBlank(message = "Secondary user mobile is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "mobile must be a 10-digit number")
    private String mobile;
    @Nullable
    @Email
    private String email;
    @NotBlank(message = "Relation is required in Secondary details")
    private String relation;

    public SecondaryDetailsDto() {
    }

    public SecondaryDetailsDto(String userName, String mobile, String email, String relation) {
        this.userName = userName;
        this.mobile = mobile;
        this.email = email;
        this.relation = relation;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
