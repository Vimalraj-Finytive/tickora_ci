package com.uniq.tms.tms_microservice.shared.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sun.jdi.request.DuplicateRequestException;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.hibernate.JDBCException;
import org.hibernate.QueryException;
import org.hibernate.exception.SQLGrammarException;
import org.postgresql.util.PSQLException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
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
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler({
            QueryException.class,
            JpaSystemException.class
    })
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<ApiResponse<Object>> handleHibernateQueryErrors(Exception ex) {

        log.error("Hibernate Query Exception caught", ex);

        String msg = ex.getMessage().toLowerCase();

        if (msg.contains("does not exist")
                || msg.contains("column")
                || msg.contains("sqlstate: 42703")
                || msg.contains("could not extract resultset")) {

            return ResponseEntity.status(500).body(
                    new ApiResponse<>(500,
                            "Missing column or table. Please contact support.",
                            null)
            );
        }

        return ResponseEntity.status(500).body(
                new ApiResponse<>(500,
                        "Database query failed. Please contact support.",
                        null)
        );
    }

    @ExceptionHandler({SQLGrammarException.class, PSQLException.class, JDBCException.class})
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public ResponseEntity<ApiResponse<Object>> handleSQLStructuralErrors(Exception ex) {
        log.error("SQL STRUCTURAL ERROR CAUGHT", ex);
        ApiResponse<Object> response = new ApiResponse<>(
                500,
                "Missing column or table. Please contact support.",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public ResponseEntity<ApiResponse<Object>> handleDataAccess(DataAccessException ex) {
        String msg = ex.getMessage().toLowerCase();
        if (msg.contains("does not exist")
                || msg.contains("sqlstate: 42703")
                || msg.contains("column")) {
            log.error("SQL ERROR wrapped in DataAccessException", ex);
            return ResponseEntity.status(500).body(
                    new ApiResponse<>(500,
                            "Missing column or table. Please contact support..",
                            null)
            );
        }

        log.error("Data access error", ex);

        return ResponseEntity.status(500).body(
                new ApiResponse<>(500, "Database operation failed.", null)
        );
    }

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
        body.put("statusCode", 400);
        body.put("message", sanitizeMessage(ex.getMessage()));
        body.put("data", null);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        log.error("Invalid state", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", 400);
        body.put("message", sanitizeMessage(ex.getMessage()));
        body.put("data", null);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException", ex);
        return new ResponseEntity<>(Map.of(
                "statusCode", ex.getStatusCode().value(),
                "message", sanitizeMessage(ex.getReason()),
                "data", null
        ), ex.getStatusCode());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDuplicateEntry(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);
        return ResponseEntity.status(409)
                .body(new ApiResponse(409, parseDataIntegrityError(ex.getMessage()), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

        String errorMessage = errors.entrySet()
                .stream()
                .map(e -> e.getKey() + " : " + e.getValue())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(new ApiResponse(400, errorMessage, null));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse(400, "User not found", null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(BadRequestException ex) {
        return new ResponseEntity<>(new ApiResponse(400, sanitizeMessage(ex.getMessage()), null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ApiResponse> handleDuplicateUserException(DuplicateRequestException ex) {
        return new ResponseEntity<>(new ApiResponse(409, "Duplicate request. Resource already exists.", null), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(new ApiResponse(400, "File size exceeds limit", null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse> handleMissingPart(MissingServletRequestPartException ex) {
        return new ResponseEntity<>(new ApiResponse(400, "Required field '" + ex.getRequestPartName() + "' is missing", null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return new ResponseEntity<>(new ApiResponse(415, "Unsupported content type", null), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse> handleInvalidFormat(InvalidFormatException ex) {
        return new ResponseEntity<>(new ApiResponse(400, sanitizeMessage(ex.getMessage()), null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiResponse> handleDateTimeParse(DateTimeParseException ex) {
        return new ResponseEntity<>(new ApiResponse(400, "Invalid time format", null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse(400, "Malformed JSON request", null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        log.error("Runtime exception", ex);
        return new ResponseEntity<>(Map.of(
                "statusCode", 500,
                "message", "An unexpected error occurred.",
                "data", null
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobal(Exception ex) {
        log.error("Unexpected exception", ex);
        return new ResponseEntity<>(new ApiResponse(500, "Unexpected error occurred.", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String sanitizeMessage(String message) {
        if (message == null) return "An error occurred";

        String sanitized = message
                .replaceAll("(?i)sql.*", "")
                .replaceAll("(?i)jdbc.*", "")
                .replaceAll("\\[.*?\\]", "")
                .trim();

        return sanitized.length() < 10
                ? "An error occurred while processing your request"
                : sanitized;
    }

    private String parseDataIntegrityError(String errorMessage) {
        if (errorMessage == null) return "Data validation failed";

        String lower = errorMessage.toLowerCase();

        if (lower.contains("duplicate")) return "Duplicate entry found";
        if (lower.contains("not-null")) return "Required field is missing";
        if (lower.contains("foreign key")) return "Referenced data does not exist";

        return "Invalid data provided";
    }
}
