package com.uniq.tms.tms_microservice.modules.timesheetManagement.controller;

import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.constant.TimesheetConstant;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.facade.TimesheetFacade;
import com.uniq.tms.tms_microservice.shared.util.ReportUtil;
import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping(TimesheetConstant.REPORT_URL)
public class ReportController {

    private final TimesheetFacade timesheetFacade;
    private final AuthHelper authHelper;

    public ReportController(TimesheetFacade timesheetFacade, AuthHelper authHelper) {
        this.timesheetFacade = timesheetFacade;
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
            timesheetFacade.generateTimesheetFileAsync(request, fileName, userIdFromToken, orgId, role, tenantId);

            return ResponseEntity.ok(
                    new ApiResponse<>(200, "File generated successfully.", fileName)
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
            log.info("Downloading file: {}", fileName);

            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return ResponseEntity.badRequest().build();
            }

            Path filePath = Paths.get(downloadDir).resolve(fileName);
            log.info("File path: {}", filePath);

            if (!Files.exists(filePath)) {
                log.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            long fileSize = Files.size(filePath);
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
            MediaType mediaType = determineMediaType(fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" +
                                    URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                    .contentType(mediaType)
                    .contentLength(fileSize)
                    .body(resource);

        } catch (IOException | java.io.IOException e) {
            log.error("Error downloading file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType determineMediaType(String fileName) {
        if (fileName.toLowerCase().endsWith(".csv")) {
            return MediaType.parseMediaType("text/csv");
        } else if (fileName.toLowerCase().endsWith(".xlsx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

}
