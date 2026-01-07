package com.uniq.tms.tms_microservice.modules.payrollManagement.controller;

import com.uniq.tms.tms_microservice.modules.payrollManagement.constant.PayRollConstant;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollSettingDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.UserPayRollUpdateDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.facade.PayRollFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
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
import java.util.List;

@RestController
@RequestMapping(PayRollConstant.PAYROLL_URl)
public class PayRollController {

    private final PayRollFacade facade;
    private final AuthHelper authHelper;

    public PayRollController(PayRollFacade facade, AuthHelper authHelper) {
        this.facade = facade;
        this.authHelper = authHelper;
    }

    @Value("${csv.payroll.download.dir}")
    private String downloadDir;

    @PutMapping("/settings/update")
    public ResponseEntity<ApiResponse<Object>> createOrUpdate(
            @RequestHeader("Authorization") String token,
            @RequestBody PayRollSettingDto dto) {
        ApiResponse<Object> result = facade.createOrUpdate(dto);
        return ResponseEntity.status(result.getStatusCode()).body(result);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Object>> PayrollDetails(
            @RequestHeader("Authorization") String token,
            @RequestBody PayRollDto dto) {
        ApiResponse<Object> created = facade.PayrollDetails(dto);
        return ResponseEntity.status(created.getStatusCode()).body(created);
    }

    @GetMapping("/amount")
    public ResponseEntity<ApiResponse<List<UserPayRollAmountDto>>> getPayrollAmount(@RequestHeader("Authorization") String token,
                                                                                    @RequestParam(required = false) String id,
                                                                                    @RequestParam String month){
        ApiResponse<List<UserPayRollAmountDto>> paymentDto = facade.getPayrollAmount(id, month);
        return ResponseEntity.status(paymentDto.getStatusCode()).body(paymentDto);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayRollSummaryDto>>> getAllPayrollIdAndName(@RequestHeader("Authorization") String token){
        ApiResponse<List<PayRollSummaryDto>> payrollDto = facade.getAllPayrollIdAndName();
        return ResponseEntity.status(payrollDto.getStatusCode()).body(payrollDto);
    }

    @GetMapping("/summary/{month}")
    public ResponseEntity<ApiResponse<PayRollPaymentSummaryDto>> getPayrollPayment(@RequestHeader("Authorization") String token,
                                                                                   @PathVariable String month){
        ApiResponse<PayRollPaymentSummaryDto> payrollPayment = facade.getPayrollPayment(month);
        return ResponseEntity.status(payrollPayment.getStatusCode()).body(payrollPayment);
    }

    @PutMapping("/userPayRoll/update/{userId}/{month}")
    public ResponseEntity<ApiResponse<UserPayRollUpdateDto>> updateUserPayroll(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId,
            @PathVariable String month,
            @RequestBody UserPayRollUpdateDto dto) {
        ApiResponse<UserPayRollUpdateDto> response =
                facade.updatePayrollAmount(userId, dto, month);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    //Not In use
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponseDto>> getPayrollById(@RequestHeader("Authorization") String token,
                                                                          @PathVariable String id) {
        ApiResponse<PayrollResponseDto> response = facade.getPayrollById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<PayrollListResponseDto>>> getAllPayrolls(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<PayrollListResponseDto>> response = facade.getAllPayrolls();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/{payrollId}/status")
    public ResponseEntity<ApiResponse<String>> updatePayrollStatus(
            @PathVariable String payrollId,
            @RequestBody PayrollStatusUpdateDto dto,
            @RequestHeader("Authorization") String token) {
        ApiResponse<String> response = facade.updatePayrollStatus(payrollId, dto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/settings/status")
    public ResponseEntity<ApiResponse<List<PayRollSettingenumDto>>> getAllSettings(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<PayRollSettingenumDto>> response = facade.getAllSettings();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<PayRollStatusEnumDto>>> getAllStatus(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<PayRollStatusEnumDto>> response = facade.getAllStatus();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<PayRollSettingDto>> getSettings(
            @RequestHeader("Authorization") String token) {
        ApiResponse<PayRollSettingDto> response = facade.getSettings();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> assignPayroll(@RequestHeader("Authorization") String token,
                                                     @RequestBody PayRollUpdateDto payRollUpdateDto){
        ApiResponse<Void> response = facade.assignPayroll(payRollUpdateDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/{payRollId}/update")
    public ResponseEntity<ApiResponse<Void>> updatePayroll(@RequestHeader("Authorization") String token,
                                                     @PathVariable String payRollId,
                                                   @RequestBody PayRollEditRequestDto payRollEditRequestDto){
        ApiResponse<Void> response=facade.updatePayroll(payRollId,payRollEditRequestDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> startExport(
            @RequestBody PayRollExportDto request) {
        String schema = authHelper.getSchema();
        String orgId = authHelper.getOrgId();
        return ResponseEntity.ok(
                facade.startExport(request.getMonth(), request.getFormat(), schema, orgId)
        );
    }

    @GetMapping("/status/{exportId}")
    public ResponseEntity<ApiResponse<String>> status(@PathVariable String exportId) {
        String schema = authHelper.getSchema();
        String orgId = authHelper.getOrgId();
        return ResponseEntity.ok(
                facade.checkStatus(schema, orgId, exportId)
        );
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadPayRoll(
            @RequestHeader("Authorization") String token,
            @RequestParam String fileName) {
        try {

            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(400, "Invalid file name", null)
                );
            }

            Path filePath = Paths.get(downloadDir).resolve(fileName);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>(404, "File not generated yet. Please try again later.", null)
                );
            }

            long fileSize = Files.size(filePath);
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
            MediaType mediaType = determineMediaType(fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName +
                                    "\"; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                    .contentType(mediaType)
                    .contentLength(fileSize)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(500, "Error reading file", null)
            );
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
