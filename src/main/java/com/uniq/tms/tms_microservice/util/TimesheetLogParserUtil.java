package com.uniq.tms.tms_microservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uniq.tms.tms_microservice.dto.PaginationDto;
import com.uniq.tms.tms_microservice.dto.PaginationResponseDto;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.service.impl.AuthServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class TimesheetLogParserUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(TimesheetLogParserUtil.class);
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static List<TimesheetHistoryDto> parseLogs(String timesheetLogsJson) {
        try {
            TypeReference<List<TimesheetHistoryDto>> typeRef = new TypeReference<List<TimesheetHistoryDto>>() {};
            return objectMapper.readValue(timesheetLogsJson, typeRef);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse timesheet logs JSON: {}", timesheetLogsJson, e);
            throw new IllegalArgumentException("Invalid timesheet logs JSON format", e);
        }
    }

    public static PaginationResponseDto emptyPagination(int pageIndex, int pageSize) {
        PaginationResponseDto resp = new PaginationResponseDto();
        resp.setStatusCode(200);
        resp.setMessage("Success");
        resp.setUserTimesheetResponseDtos(Collections.emptyList());

        PaginationDto paginationDto = new PaginationDto();
        paginationDto.setPageIndex(pageIndex);
        paginationDto.setPageSize(pageSize);
        paginationDto.setTotalElements(0);
        paginationDto.setTotalPages(0);
        paginationDto.setLast(true);

        resp.setPaginationDto(paginationDto);

        return resp;
    }


}