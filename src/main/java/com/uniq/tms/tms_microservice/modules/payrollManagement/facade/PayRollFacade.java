package com.uniq.tms.tms_microservice.modules.payrollManagement.facade;


import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.payrollManagement.mapper.PayRollDtoMapper;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.*;
import com.uniq.tms.tms_microservice.modules.payrollManagement.services.PayRollService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PayRollFacade {

    private final PayRollService service;
    private final PayRollDtoMapper dtoMapper;
    private final AuthHelper authHelper;
    private final PayRollDtoMapper payRollDtoMapper;

    public PayRollFacade(PayRollService service, PayRollDtoMapper dtoMapper,
                         AuthHelper authHelper, PayRollDtoMapper payRollDtoMapper) {
        this.service = service;
        this.dtoMapper = dtoMapper;
        this.authHelper = authHelper;
        this.payRollDtoMapper = payRollDtoMapper;
    }

    public ApiResponse<Object> createOrUpdate(PayRollSettingDto dto) {
        PayRollSettingModel model = dtoMapper.fromDto(dto);
        service.createOrUpdate(model);
        return new ApiResponse<>(200, "Payroll settings saved successfully", null);
    }

    public ApiResponse<Object> PayrollDetails(PayRollDto dto) {
        String orgId = authHelper.getOrgId();
        PayRollModel model = dtoMapper.fromDto(dto);
        service.createRecord(model, orgId);
        return new ApiResponse<>(201, "Payroll details saved successfully", null);
    }

    public ApiResponse<List<UserPayRollAmountDto>> getPayrollAmount(String id, String month) {
        List<UserPayRollAmountDto> dto = dtoMapper.toDto(service.getPayrollAmount(id, month));
        return new ApiResponse<>(200, "Fetched UserPayrollAmount Successfully", dto);
    }

    public ApiResponse<UserPayRollUpdateDto> updatePayrollAmount(UserPayRollUpdateDto dto,String month) {
        UserPayRollAmountModel model = dtoMapper.toModel(dto);
        UserPayRollAmountModel updatedModel = service.updatePayrollAmount(model,month);
        return new ApiResponse<>(200, "Payroll updated successfully", null);
    }

    public ApiResponse<PayrollResponseDto> getPayrollById(String id) {
        PayRollResponseModel model = service.getPayrollById(id);
        PayrollResponseDto dto = payRollDtoMapper.toDto(model);
        return new ApiResponse<>(200, "Payroll fetched successfully", dto);
    }

    public ApiResponse<List<PayrollListResponseDto>> getAllPayrolls() {
        List<PayRollListModel> models = service.getAllPayrolls();
        List<PayrollListResponseDto> dtos = models.stream()
                .map(payRollDtoMapper::toListDto)
                .toList();
        return new ApiResponse<>(200, "Payroll list fetched successfully", dtos);
    }

    public ApiResponse<String> updatePayrollStatus(String payrollId, PayrollStatusUpdateDto dto) {

        try {
            PayrollStatusUpdateModel model = payRollDtoMapper.toStatusUpdateModel(dto);
            boolean isActive = model.isActive();
            service.updatePayrollStatus(payrollId, model);
            String message = isActive ? "Payroll activated successfully" : "Payroll inactivated successfully";
            return new ApiResponse<>(200, message, null);
        } catch (Exception e) {
            return new ApiResponse<>(400, "Failed to update payroll: " + e.getMessage(), null
            );
        }
    }


    public ApiResponse<List<PayRollSummaryDto>> getAllPayrollIdAndName(){
        List<PayRollSummaryDto> dto = dtoMapper.toSummaryDto(service.getAllPayrollIdAndName());
        return new ApiResponse<>(200, "Fetched Payroll List Successfully",dto);
    }

    public ApiResponse<PayRollPaymentSummaryDto> getPayrollPayment(String month){
        PayRollPaymentSummaryDto dto = dtoMapper.toPayrollPaymentDto(service.getPayrollPayment(month));
        return  new ApiResponse<>(200,"Fetched Payroll Payment successfully",dto);
    }


    public ApiResponse<List<PayRollSettingenumDto>> getAllSettings() {
        List<PayRollSettingenumModel> models = service.getAllSettings();
        List<PayRollSettingenumDto> dtos = models.stream()
                .map(dtoMapper::toDto)
                .toList();
        return new ApiResponse<>(200, "SettingsEnum Fetched Successfully", dtos);
    }


    public ApiResponse<List<PayRollStatusEnumDto>> getAllStatus() {
        List<PayRollStatusEnumModel> models = service.getAllStatus();
        List<PayRollStatusEnumDto> dtos = models.stream()
                .map(dtoMapper::toDto)
                .toList();
        return new ApiResponse<>(200, "StatusEnum Fetched Successfully", dtos);
    }


    public ApiResponse<PayRollSettingDto> getSettings() {
        PayRollSettingModel model = service.getSetting();
        PayRollSettingDto dto = dtoMapper.toDto(model);
        return new ApiResponse<>(200, "PayRoll Settings fetched successfully", dto);
    }

    public ApiResponse updatePayroll(PayRollUpdateDto payRollUpdateDto){
        service.updatePayroll(dtoMapper.toPayRollUpdateModel(payRollUpdateDto));
        return new ApiResponse(200, "PayRoll Updated successfully",null);
    }

    public ApiResponse editPayroll(PayRollEditRequestDto payRollEditRequestDto) {
        try {
            service.editPayroll(dtoMapper.toEditModel(payRollEditRequestDto));
            return new ApiResponse(200, "Payroll details saved successfully", null);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400,e.getMessage(),null);
        } catch (Exception e) {
            return new ApiResponse<>(404,e.getMessage(),null);
        }
    }
}
