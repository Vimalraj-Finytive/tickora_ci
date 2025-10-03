package com.uniq.tms.tms_microservice.modules.userManagement.model;

public class SecondaryDetails {
    private String userName;
    private String email;
    private String mobile;
    private String relation;
    private Long studentId;

    public SecondaryDetails() {
    }

    public SecondaryDetails(String email, String mobile, String relation, Long studentId, String userName) {

        this.email = email;
        this.mobile = mobile;
        this.relation = relation;
        this.studentId = studentId;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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
}
