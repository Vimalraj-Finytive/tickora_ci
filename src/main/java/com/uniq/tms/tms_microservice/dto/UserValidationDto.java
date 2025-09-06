package com.uniq.tms.tms_microservice.dto;

import com.uniq.tms.tms_microservice.enums.LogType;
import java.util.List;

public class UserValidationDto {

    private String userName;
    private String userId;
    private boolean isRegisterUser;
    private LogType logType;
    private List<LocationDto> location;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRegisterUser() {
        return isRegisterUser;
    }

    public void setRegisterUser(boolean registerUser) {
        isRegisterUser = registerUser;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public List<LocationDto> getLocation() {
        return location;
    }

    public void setLocation(List<LocationDto> location) {
        this.location = location;
    }
}
