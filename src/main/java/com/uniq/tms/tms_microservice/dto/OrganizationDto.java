package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizationDto {

    private String orgName;
    private String orgType;
    private Integer orgSize;
    private String country;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String mobile;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getOrgSize() {
        return orgSize;
    }

    public void setOrgSize(Integer orgSize) {
        this.orgSize = orgSize;
    }
}
