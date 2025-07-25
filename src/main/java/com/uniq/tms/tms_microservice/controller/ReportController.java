package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.FileExportResponseDto;
import com.uniq.tms.tms_microservice.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import io.jsonwebtoken.io.IOException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping(UserConstant.REPORT_URL)
public class ReportController {

    private final AuthFacade authFacade;

    public ReportController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/timesheets")
    public ResponseEntity<InputStreamResource> getTimesheets(
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) TimesheetReportDto request) {

        try {
            FileExportResponseDto export = authFacade.generateTimesheetFile(request);

            InputStreamResource resource = new InputStreamResource(Files.newInputStream(export.getFilePath()));
            MediaType mediaType = export.getFormat().equalsIgnoreCase("csv") ?
                    MediaType.parseMediaType("text/csv") :
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + export.getFileName() + "\"; filename*=UTF-8''" +
                                    URLEncoder.encode(export.getFileName(), StandardCharsets.UTF_8))
                    .contentType(mediaType)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
