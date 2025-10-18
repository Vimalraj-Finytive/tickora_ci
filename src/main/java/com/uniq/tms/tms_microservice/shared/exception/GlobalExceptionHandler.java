package com.uniq.tms.tms_microservice.shared.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sun.jdi.request.DuplicateRequestException;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.AccessDeniedException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage(), ex);
        Map<String, String> response = new HashMap<>();
        response.put("error", "Access Denied");
        response.put("message", "You don't have permission to access this resource");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", HttpStatus.BAD_REQUEST.value());
        body.put("message", sanitizeMessage(ex.getMessage()));
        body.put("data", null);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        log.error("Response status exception: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", ex.getStatusCode().value());
        body.put("message", sanitizeMessage(ex.getReason()));
        body.put("data", null);
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDuplicateEntryException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        String userMessage = parseDataIntegrityError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(409, userMessage, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put("Error", error.getDefaultMessage()));
        String errorMessage = String.join(", ", errors.values());
        return ResponseEntity.badRequest().body(new ApiResponse(400, errorMessage, false));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFoundException(UsernameNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(new ApiResponse(400, "User not found", false));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("message", "An unexpected error occurred. Please try again later.");
        body.put("data", null);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(500, "An unexpected error occurred. Please contact support if the issue persists.", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequestException(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(400, sanitizeMessage(ex.getMessage()), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ApiResponse> handleDuplicateUserException(DuplicateRequestException ex) {
        log.error("Duplicate request: {}", ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(409, "Duplicate request. Resource already exists.", null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(400, "File size exceeds the maximum allowed limit", null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse> handleMissingPart(MissingServletRequestPartException ex) {
        log.error("Missing request part: {}", ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(400, "Required field '" + ex.getRequestPartName() + "' is missing", null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        log.error("Media type not supported: {}", ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(415, "Content type not supported. Please use multipart/form-data for file uploads", null);
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(CommonExceptionHandler.ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflictException(CommonExceptionHandler.ConflictException ex) {
        log.error("Conflict exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(409, sanitizeMessage(ex.getMessage()), null));
    }

    @ExceptionHandler(CommonExceptionHandler.InternalServerException.class)
    public ResponseEntity<ApiResponse> handleInternalServerException(CommonExceptionHandler.InternalServerException ex) {
        log.error("Internal server exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(500, "Internal server error occurred. Please try again later.", null));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse> handleInvalidFormatException(InvalidFormatException ex) {
        log.error("Media type not supported: {}", ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(400, sanitizeMessage(ex.getMessage()), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiResponse> handleDateTimeParseException(DateTimeParseException ex) {
        log.error("Invalid request: {}", ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(500,"Invalid Time Format", null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Invalid JSON input: {}", ex.getMessage());
        Throwable cause = ex.getCause();
        String message = "Malformed or invalid JSON request";
        if (cause instanceof InvalidFormatException invalidFormatEx) {
            message = "Invalid value '" + invalidFormatEx.getValue() +
                    "' for field '" + invalidFormatEx.getPath().getFirst().getFieldName() + "'";
        }
        ApiResponse response = new ApiResponse(400, message, null);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Sanitize error messages to remove sensitive information
     */
    private String sanitizeMessage(String message) {
        if (message == null) {
            return "An error occurred";
        }

        String sanitized = message.replaceAll("(?i)sql.*?\\[.*?\\]", "")
                .replaceAll("(?i)constraint.*?\\[.*?\\]", "")
                .replaceAll("(?i)relation \".*?\"", "table")
                .replaceAll("(?i)column \".*?\"", "field")
                .trim();

        if (sanitized.isEmpty() || sanitized.length() < 10) {
            return "An error occurred while processing your request";
        }

        return sanitized;
    }

    private String parseDataIntegrityError(String errorMessage) {
        if (errorMessage == null) {
            return "Data validation failed";
        }

        String lowerCaseError = errorMessage.toLowerCase();

        if (lowerCaseError.contains("null value") && lowerCaseError.contains("violates not-null constraint")) {
            if (lowerCaseError.contains("mobile_number")) {
                return "Mobile number is required";
            } else if (lowerCaseError.contains("email")) {
                return "Email is required";
            }
            return "Required field is missing";
        }

        if (lowerCaseError.contains("duplicate key value") || lowerCaseError.contains("violates unique constraint")) {
            if (lowerCaseError.contains("email")) {
                return "Email already exists";
            } else if (lowerCaseError.contains("mobile_number")) {
                return "Mobile number already exists";
            } else if (lowerCaseError.contains("face_id")) {
                return "Face ID already registered";
            }
            return "Record already exists";
        }

        if (lowerCaseError.contains("foreign key constraint")) {
            return "Referenced data does not exist";
        }
        if (lowerCaseError.contains("check constraint")) {
            return "Data validation failed - invalid value provided";
        }

        return "Data validation failed. Please check your input and try again";
    }

}
