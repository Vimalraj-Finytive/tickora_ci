package com.uniq.tms.tms_microservice.modules.authenticationManagement.model;

public class EmailData {
    private String email;
    private String userName;
    private String generatedPass;
    private boolean isNewUser;
    private Long roleId;

    // Constructor
    public EmailData(String email, String userName, String generatedPass, boolean isNewUser, Long roleId) {
        this.email = email;
        this.userName = userName;
        this.generatedPass = generatedPass;
        this.isNewUser = isNewUser;
        this.roleId = roleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGeneratedPass() {
        return generatedPass;
    }

    public void setGeneratedPass(String generatedPass) {
        this.generatedPass = generatedPass;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setRegisterUser(boolean isNewUser) {
        this.isNewUser = isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
