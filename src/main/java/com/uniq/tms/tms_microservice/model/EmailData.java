package com.uniq.tms.tms_microservice.model;

public class EmailData {
    private String email;
    private String userName;
    private String generatedPass;
    private boolean isNewUser;

    // Constructor


    public EmailData(String email, String userName, String generatedPass, boolean isNewUser) {
        this.email = email;
        this.userName = userName;
        this.generatedPass = generatedPass;
        this.isNewUser = isNewUser;
    }

    // Getters and Setters
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
}
