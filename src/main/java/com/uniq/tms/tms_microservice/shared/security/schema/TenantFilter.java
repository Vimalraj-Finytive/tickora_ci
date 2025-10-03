package com.uniq.tms.tms_microservice.shared.security.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.shared.security.cache.CachedBodyHttpServletRequest;
import com.uniq.tms.tms_microservice.shared.security.jwt.JwtFilter;
import com.uniq.tms.tms_microservice.shared.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private final Logger log = LogManager.getLogger(TenantFilter.class);
    private final DataSource dataSource;
    private final JwtFilter jwtFilter;
    private final JwtUtil jwtUtil;

    public TenantFilter(DataSource dataSource, JwtFilter jwtFilter, JwtUtil jwtUtil) {
        this.dataSource = dataSource;
        this.jwtFilter = jwtFilter;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String tenantId = null;

        String token = jwtFilter.extractTokenFromRequest(request);
        if (token != null) {
            try {
                Claims claims = jwtUtil.extractAllClaims(token);
                tenantId = claims.get("userSchema", String.class);
                if (tenantId != null && !tenantId.isBlank()) {
                    log.info("Resolved tenant from JWT token: {}", tenantId);
                } else {
                    log.warn("JWT token present but userSchema claim missing, falling back");
                }
            } catch (Exception e) {
                log.error("Failed to extract tenant from JWT token, falling back", e);
                throw new RuntimeException(e);
            }
        }

        if (tenantId == null || tenantId.isBlank()) {
            tenantId = request.getHeader(TENANT_HEADER);

            String mobileParam = request.getParameter("mobile");
            if ((tenantId == null || tenantId.isBlank()) && mobileParam != null && !mobileParam.isBlank()) {
                tenantId = getUserSchemaByMobileDirectJDBC(mobileParam,response);
                log.info("Resolved tenant from mobile param {}: {}", mobileParam, tenantId);
                if (tenantId == null) {
                    return;
                }
            }

            String emailParam = request.getParameter("email");
            if ((tenantId == null || tenantId.isBlank()) && emailParam != null && !emailParam.isBlank()) {
                tenantId = getUserSchemaByEmailDirectJDBC(emailParam, response);
                log.info("Resolved tenant from email param {}: {}", emailParam, tenantId);
                if (tenantId == null) {
                    return;
                }
            }
        }

        boolean isMultipart = request.getContentType() != null &&
                request.getContentType().toLowerCase().startsWith("multipart/");
        HttpServletRequest effectiveRequest = request;

        if (!isMultipart) {
            effectiveRequest = new CachedBodyHttpServletRequest(request);

            if ((tenantId == null || tenantId.isBlank())
                    && "POST".equalsIgnoreCase(effectiveRequest.getMethod())
                    && (effectiveRequest.getRequestURI().contains("/loginByEmail")
                    || effectiveRequest.getRequestURI().contains("/validate-email")
                    || effectiveRequest.getRequestURI().contains("/loginByMobile"))) {

                String requestBody = ((CachedBodyHttpServletRequest) effectiveRequest).getCachedBodyAsString();
                log.info("Request Body in Filter: {}", requestBody);

                if (!requestBody.isBlank()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(requestBody);

                        if ((tenantId == null || tenantId.isBlank()) && jsonNode.has("email")) {
                            String emailBody = jsonNode.get("email").asText();
                            tenantId = getUserSchemaByEmailDirectJDBC(emailBody,response);
                            log.info("Resolved tenant from JSON email {}: {}", emailBody, tenantId);
                        }

                        if ((tenantId == null || tenantId.isBlank()) && jsonNode.has("mobile")) {
                            String mobileBody = jsonNode.get("mobile").asText();
                            tenantId = getUserSchemaByMobileDirectJDBC(mobileBody,response);
                            log.info("Resolved tenant from JSON mobile {}: {}", mobileBody, tenantId);
                        }
                    } catch (Exception e) {
                        log.error("Error parsing request body for tenant resolution", e);
                    }
                }
            }
        } else {
            log.debug("Skipping request body caching for multipart upload");
        }

        if (tenantId == null || tenantId.isBlank()) {
            tenantId = "public";
        }

        log.info("TenantFilter - setting tenant to: {}", tenantId);
        TenantContext.setCurrentTenant(tenantId);

        try {
            filterChain.doFilter(effectiveRequest, response);
        } finally {
            TenantContext.clear();
        }
    }

    /** Get schema by email directly using JDBC */
    private String getUserSchemaByEmailDirectJDBC(String email, HttpServletResponse response) throws IOException {
        String sql = "SELECT schema_name FROM public.user_schema_mapping WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (Statement schemaStatement = connection.createStatement()) {
                schemaStatement.execute("SET search_path TO public");
            }
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String schemaName = resultSet.getString("schema_name");
                    log.info("Found schema for user {}: {}", email, schemaName);
                    return schemaName;
                }
            }
            log.warn("No schema mapping found for email: {}", email);
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found for email");
            response.flushBuffer();
            return null;
        } catch (SQLException e) {
            log.error("Error finding schema for email using direct JDBC: {}", email, e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error while fetching schema for email: " + email);
            response.flushBuffer();
            return null;
        }
    }

    /** Get schema by mobile directly using JDBC */
    private String getUserSchemaByMobileDirectJDBC(String mobile, HttpServletResponse response) throws IOException {
        String sql = "SELECT schema_name FROM public.user_schema_mapping WHERE mobile_number = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (Statement schemaStatement = connection.createStatement()) {
                schemaStatement.execute("SET search_path TO public");
            }
            statement.setString(1, mobile);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String schemaName = resultSet.getString("schema_name");
                    log.info("Found schema for user {}: {}", mobile, schemaName);
                    return schemaName;
                }
            }
            log.warn("No schema mapping found for mobile: {}", mobile);
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found for Given Mobile");
            response.flushBuffer();
            return null;
        } catch (SQLException e) {
            log.error("Error finding schema for mobile using direct JDBC: {}", mobile, e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error while fetching schema for mobile: " + mobile);
            response.flushBuffer();
            return null;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        if (response.isCommitted()) {
            log.warn("Response already committed. Skipping error response.");
            return;
        }
        response.resetBuffer();
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String json = String.format("{\"status\":%d,\"message\":\"%s\",\"data\":null}",
                status, message.replace("\"", "\\\""));
        response.getWriter().write(json);
        response.flushBuffer();
    }

}
