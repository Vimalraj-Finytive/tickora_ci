package com.uniq.tms.tms_microservice.helper;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ExceptionHelper {

    public String getUserFriendlyConstraintMessage(ConstraintViolationException e) {
        String constraint = e.getConstraintName();
        if (constraint != null) {
            return switch (constraint) {
                case "user_schema_mapping_email_key" ->
                        "Email already exists. Please use a different email address.";
                case "user_schema_mapping_mobile_number_key" ->
                        "Mobile number already exists. Please use a different mobile number.";
                case "user_schema_mapping_org_id_key" ->
                        "Organization already exists with this configuration.";
                default ->
                        "Duplicate entry detected. Please check your input data.";
            };
        }
        return "A database constraint was violated during organization creation.";
    }

    public String extractConstraintMessage(DataIntegrityViolationException e) {
        String message = e.getMessage();
        if (message.contains("user_schema_mapping_email_key")) {
            return "Email already exists. Please use a different email address.";
        } else if (message.contains("user_schema_mapping_mobile_number_key")) {
            return "Mobile number already exists. Please use a different mobile number.";
        } else if (message.contains("user_schema_mapping_org_id_key")) {
            return "Organization already exists with this configuration.";
        }
        return "Data integrity violation occurred during organization creation.";
    }

}
