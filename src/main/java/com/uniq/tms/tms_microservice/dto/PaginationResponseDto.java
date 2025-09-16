package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class PaginationResponseDto {

    private int statusCode;
    private String message;
    private Object data;
    private PaginationDto paginationDto;
    private List<UserTimesheetResponseDto> userTimesheetResponseDtos;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public PaginationDto getPaginationDto() {
        return paginationDto;
    }

    public void setPaginationDto(PaginationDto paginationDto) {
        this.paginationDto = paginationDto;
    }

    public List<UserTimesheetResponseDto> getUserTimesheetResponseDtos() {
        return userTimesheetResponseDtos;
    }

    public void setUserTimesheetResponseDtos(List<UserTimesheetResponseDto> userTimesheetResponseDtos) {
        this.userTimesheetResponseDtos = userTimesheetResponseDtos;
    }
}
