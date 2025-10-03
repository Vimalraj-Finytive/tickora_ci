package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import jakarta.validation.constraints.NotNull;

public class CreateUserDto {
    @NotNull
    private UserDto user;
    private SecondaryDetailsDto secondaryDetails;
    // getters and setters

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public SecondaryDetailsDto getSecondaryDetails() {
        return secondaryDetails;
    }

    public void setSecondaryDetails(SecondaryDetailsDto secondaryDetails) {
        this.secondaryDetails = secondaryDetails;
    }
}
