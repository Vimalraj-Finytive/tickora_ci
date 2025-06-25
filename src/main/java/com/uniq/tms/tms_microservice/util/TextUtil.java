package com.uniq.tms.tms_microservice.util;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TextUtil {

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
}
