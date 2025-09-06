package com.uniq.tms.tms_microservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.service.impl.AuthServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}