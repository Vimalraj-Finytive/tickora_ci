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
    public ResponseEntity<ApiResponse<Object>> handleHibernateQueryErrors(Exception ex) {

        log.error("Hibernate Query Exception caught", ex);

        String msg = safe(ex.getMessage()).toLowerCase();

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
    public ResponseEntity<ApiResponse<Object>> handleSQLStructuralErrors(Exception ex) {

        log.error("SQL STRUCTURAL ERROR CAUGHT", ex);

        return ResponseEntity.status(500)
                .body(new ApiResponse<>(500,
                        "Missing column or table. Please contact support.",
                        null));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataAccess(DataAccessException ex) {
        String msg = safe(ex.getMessage()).toLowerCase();
        if (msg.contains("does not exist")
                || msg.contains("sqlstate: 42703")
                || msg.contains("column")) {
            log.error("SQL ERROR wrapped in DataAccessException", ex);

            return ResponseEntity.status(500).body(
                    new ApiResponse<>(500,
                            "Missing column or table. Please contact support.",
                            null)
            );
        }
        log.error("Data access error", ex);
        return ResponseEntity.status(500).body(
                new ApiResponse<>(500, "Database operation failed.", null)
        );
    }

    @ExceptionHandler(CommonExceptionHandler.SchemaNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleSchemaNotFound(CommonExceptionHandler.SchemaNotFoundException ex) {
        log.error("Schema not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, safe(ex.getMessage()), null));
    }

    @ExceptionHandler(CommonExceptionHandler.NoUserLocationAssignedException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoUserLocationAssigned(CommonExceptionHandler.NoUserLocationAssignedException ex) {
        log.error("No user location assigned: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(400, safe(ex.getMessage()), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Access Denied");
        response.put("message", "You don't have permission to access this resource");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, "User not found", null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage(), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", 400);
        body.put("message", safe(ex.getMessage()));
        body.put("data", null);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        log.error("Invalid state: {}", ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", 400);
        body.put("message", safe(ex.getMessage()));
        body.put("data", null);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", ex.getStatusCode().value());
        body.put("message", safe(ex.getReason()));
        body.put("data", null);

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateEntry(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);

        return ResponseEntity.status(409)
                .body(new ApiResponse<>(409,
                        parseDataIntegrityError(ex.getMessage()),
                        null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, errorMessage, null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, safe(ex.getMessage()), null));
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateRequest(DuplicateRequestException ex) {
        return ResponseEntity.status(409)
                .body(new ApiResponse<>(409, "Duplicate request. Resource already exists.", null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, "File size exceeds limit", null));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingPart(MissingServletRequestPartException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, "Required field '" + ex.getRequestPartName() + "' is missing", null));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ApiResponse<>(415, "Unsupported content type", null));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidFormat(InvalidFormatException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, safe(ex.getMessage()), null));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiResponse<Object>> handleDateTimeParse(DateTimeParseException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, "Invalid time format", null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, "Malformed JSON request", null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        log.error("Runtime exception", ex);

        Map<String, Object> body = new HashMap<>();
        body.put("statusCode", 500);
        body.put("message", safe(ex.getMessage()));
        body.put("data", null);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobal(Exception ex) {
        log.error("Unexpected exception", ex);

        return ResponseEntity.status(500)
                .body(new ApiResponse<>(500, "Unexpected error occurred.", null));
    }

    private String safe(String msg) {
        if (msg == null) return "An error occurred";
        try {
            msg = msg.replaceAll("(?i)sql.*", "")
                    .replaceAll("(?i)jdbc.*", "")
                    .replaceAll("\\[.*?\\]", "")
                    .trim();
            if (msg.isBlank()) return "An error occurred";
            return msg;
        } catch (Exception e) {
            return "An error occurred";
        }
    }

    private String parseDataIntegrityError(String msg) {
        msg = safe(msg).toLowerCase();

        if (msg.contains("duplicate")) return "Duplicate entry found";
        if (msg.contains("not-null")) return "Required field is missing";
        if (msg.contains("foreign key")) return "Referenced data does not exist";

        return "Invalid data";
    }
}
