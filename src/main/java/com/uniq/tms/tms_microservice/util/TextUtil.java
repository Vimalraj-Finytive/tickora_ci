package com.uniq.tms.tms_microservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.dto.UserGroupDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class TextUtil {

    private static final Logger log = LogManager.getLogger(TextUtil.class);
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Returns true if the input is null, blank, "null", or "[null]" - String type.
     */
    public static boolean isBlank(String input) {
        return input == null
                || input.trim().isEmpty()
                || input.equalsIgnoreCase("null")
                || input.equalsIgnoreCase("[null]");
    }

    /**
     *  Trims the input and returns null if the result is blank, "null", or "[null]".
     */
    public static String trim(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        return isBlank(trimmed) ? null : trimmed;
    }

    /**
     * Returns true if the input is null, blank, "null", or "[null]" - Long type.
     */
    public static boolean isBlank(Long input){
        return input == null ;
    }

    /**
     * Return trune if the provided list of Ids is null or empty
     */
    public static boolean isBlank(List<Long> input){
        return input == null || input.isEmpty();
    }

    public List<UserGroupDto> parseMembers(String json) {
        try {
            if (json == null || json.isBlank()) return Collections.emptyList();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse membersDetails JSON: {}", e.getMessage(), e);
            return Collections.emptyList(); // Or throw if you want stricter handling
        }
    }

}
