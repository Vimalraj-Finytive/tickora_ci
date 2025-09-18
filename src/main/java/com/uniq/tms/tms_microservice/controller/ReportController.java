package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import com.uniq.tms.tms_microservice.helper.AuthHelper;
import com.uniq.tms.tms_microservice.util.ReportUtil;
import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping(UserConstant.REPORT_URL)
public class ReportController {

    private final AuthFacade authFacade;
    private final AuthHelper authHelper;

    public ReportController(AuthFacade authFacade, AuthHelper authHelper) {
        this.authFacade = authFacade;
        this.authHelper = authHelper;
    }

    @Value("${csv.download.dir}")
    private String downloadDir;

    @PostMapping("/timesheets/generate")
    public ResponseEntity<ApiResponse> generateTimesheet(
            @RequestHeader("Authorization") String token,
            @RequestBody TimesheetReportDto request) {

        try {
            String orgId = authHelper.getOrgId();
            String userIdFromToken = authHelper.getUserId();
            String role = authHelper.getRole();
            String tenantId = authHelper.getSchema();
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(401, "Unauthorized - Invalid Organization", null));
            }

            String fileName = ReportUtil.generateFileName(request, downloadDir);
            authFacade.generateTimesheetFileAsync(request, fileName, userIdFromToken, orgId, role, tenantId);

            return ResponseEntity.ok(
                    new ApiResponse<>(200, "File generation started.", fileName)
            );

        } catch (Exception e) {
            log.error("Error while scheduling file generation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error while scheduling file generation", null));
        }
    }

    @GetMapping("/timesheets/download")
    public ResponseEntity<InputStreamResource> downloadTimesheet(
            @RequestHeader("Authorization") String token,
            @RequestParam String fileName) {

        try {
            log.info("get file path");
            Path filePath = Paths.get(downloadDir).resolve(fileName);
            log.info("File path : {}", filePath);
            if (!Files.exists(filePath)) {
                log.info("File not Exist");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            log.info("File Exists.");
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

            MediaType mediaType;
            if (fileName.endsWith(".csv")) {
                mediaType = MediaType.parseMediaType("text/csv");
            } else if (fileName.endsWith(".xlsx")) {
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .contentType(mediaType)
                    .contentLength(Files.size(filePath))
                    .body(resource);

        } catch (IOException | java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
