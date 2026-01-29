package com.uniq.tms.tms_microservice.modules.ReportManagement.controller;

import com.uniq.tms.tms_microservice.modules.ReportManagement.constant.ReportConstant;
import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import com.uniq.tms.tms_microservice.modules.ReportManagement.facade.ReportFacade;
import com.uniq.tms.tms_microservice.modules.ReportManagement.helper.ReportDownloadResolver;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffExportRequestDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollExportDto;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetReportDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

@RestController
@RequestMapping(ReportConstant.REPORT_URL)
public class ReportController {

    private static final Logger log = LogManager.getLogger(ReportController.class);

    private final ReportFacade reportFacade;
    private final ReportDownloadResolver downloadResolver;

    public ReportController(ReportFacade reportFacade, ReportDownloadResolver downloadResolver) {
        this.reportFacade = reportFacade;
        this.downloadResolver = downloadResolver;
    }

    @PostMapping("/generate/timesheet")
    public ResponseEntity<ApiResponse<String>> generateReport(@RequestBody TimesheetReportDto reportDto){
        return ResponseEntity.accepted()
                .body(reportFacade.start(ReportType.TIMESHEET, reportDto));
    }

    @PostMapping("/generate/payRoll")
    public ResponseEntity<ApiResponse<String>> startExport(
            @RequestBody PayRollExportDto request) {
        return ResponseEntity.accepted()
                .body(reportFacade.start(ReportType.PAYROLL, request));
    }

    @PostMapping("/generate/timeoffRequest")
    public ResponseEntity<ApiResponse<String>> startExport(@RequestBody TimeOffExportRequestDto request) {
        return ResponseEntity.accepted()
                .body(reportFacade.start(ReportType.TIMEOFF_REQUEST, request));
    }

    @GetMapping("/status/{type}/{exportId}")
    public ApiResponse<String> status(
            @PathVariable ReportType type,
            @PathVariable String exportId) {
        return reportFacade.status(type, exportId);
    }

    @GetMapping("/download/{type}")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable ReportType type,
            @RequestParam String fileName) {

        try {
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return ResponseEntity.badRequest().build();
            }

            Path filePath = downloadResolver.resolve(type, fileName);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            InputStreamResource resource =
                    new InputStreamResource(Files.newInputStream(filePath));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" +
                                    URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                    .contentType(determineMediaType(fileName))
                    .contentLength(Files.size(filePath))
                    .body(resource);

        } catch (Exception e) {
            log.error("Download failed", e);
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
