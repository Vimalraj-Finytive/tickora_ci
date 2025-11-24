package com.uniq.tms.tms_microservice.modules.payrollManagement.controller;

import com.uniq.tms.tms_microservice.modules.payrollManagement.constant.PayRollConstant;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollSettingDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.UserPayRollUpdateDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.facade.PayRollFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(PayRollConstant.PAYROLL_URl)
public class PayRollController {

    private PayRollFacade facade;

    public PayRollController(PayRollFacade facade) {
        this.facade = facade;
    }

    @PutMapping("/setting/create")
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

    @GetMapping("/{id}/{month}")
    public ResponseEntity<ApiResponse<List<UserPayRollAmountDto>>> getPayrollAmount(@RequestHeader("Authorization") String token,
                                                                                    @PathVariable String id, @PathVariable String month){
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

    @PutMapping("/userPayRoll/update/{month}")
    public ResponseEntity<ApiResponse<UserPayRollUpdateDto>> updatePayroll(
            @RequestHeader("Authorization") String token,
            @RequestBody UserPayRollUpdateDto dto,@PathVariable String month) {
        ApiResponse<UserPayRollUpdateDto> response = facade.updatePayrollAmount(dto,month);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponseDto>> getPayrollById(@RequestHeader("Authorization") String token,@PathVariable String id) {
        ApiResponse<PayrollResponseDto> response = facade.getPayrollById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/amount/details")
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
    public ResponseEntity<ApiResponse> updatePayroll(@RequestHeader("Authorization") String token, @RequestBody PayRollUpdateDto payRollUpdateDto){
        ApiResponse response = facade.updatePayroll(payRollUpdateDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/edit")
    public ResponseEntity<ApiResponse> editPayRoll(@RequestHeader("Authorization") String token,@RequestBody PayRollEditRequestDto payRollEditRequestDto){
        ApiResponse response=facade.editPayroll(payRollEditRequestDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
