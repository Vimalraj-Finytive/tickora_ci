package com.uniq.tms.tms_microservice.modules.leavemanagement.facade;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AdminStatusUpdateDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.EmployeeStatusUpdateDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AdminStatusUpdate;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.EmployeeStatusUpdate;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequest;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffPolicyService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AccrualTypeEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CompensationEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPoliciesDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPolicyDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AccrualTypeEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CompensationEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffPolicyService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class TimeOffPoliciesFacade {

    private final TimeOffPolicyService timeOffPolicyService;
    private final TimeOffPolicyDtoMapper timeoffPolicyDtoMapper;
    private final TimeOffPolicyEntityMapper timeOffPolicyEntityMapper;

    public TimeOffPoliciesFacade(TimeOffPolicyService timeOffPolicyService, TimeOffPolicyDtoMapper timeoffPolicyDtoMapper, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper) {
        this.timeOffPolicyService = timeOffPolicyService;
        this.timeoffPolicyDtoMapper = timeoffPolicyDtoMapper;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
    }

    public ApiResponse createRequest(TimeOffRequestDto requestDto) {
        TimeOffRequest request = timeoffPolicyDtoMapper.toRequestModel(requestDto);
        timeOffPolicyService.createRequest(request);
        return new ApiResponse<>(200,"Requested TimeOff Successfully",null);
    }

    public ApiResponse employeeUpdateStatus(EmployeeStatusUpdateDto dto){
        EmployeeStatusUpdate model = timeoffPolicyDtoMapper.toStatusModel(dto);
        timeOffPolicyService.employeeUpdateStatus(model);
        return new ApiResponse<>(200,"Update TimeOff Request Successfully",null);
    }

    public ApiResponse adminUpdateStatus(AdminStatusUpdateDto dto) {
        AdminStatusUpdate model = timeoffPolicyDtoMapper.toAdminStatusModel(dto);
        timeOffPolicyService.adminUpdateStatus(model);
        return new ApiResponse<>(200, "Update TimeOff Request status Successfully", null);
    }

    public ApiResponse<TimeOffPolicyResponseDto> createPolicy(TimeOffPolicyRequestDto requestDto) {
        try {
            TimeOffPolicyRequestModel requestModel = timeoffPolicyDtoMapper.toRequestModel(requestDto);
            TimeOffPolicyResponseModel responseModel = timeOffPolicyService.createPolicy(requestModel);
            TimeOffPolicyResponseDto responseDto = timeoffPolicyDtoMapper.toResponseDto(responseModel);

            return new ApiResponse<>(200, "Policy Created Successfully", responseDto);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(500, "An unexpected error occurred", null);
        }
    }


    public ApiResponse<EntitledTypeDropdownDto> getDropDowns() {

        try {
            EntitledTypeDropdownModel model = timeOffPolicyService.getDropDowns();
            EntitledTypeDropdownDto dto = timeoffPolicyDtoMapper.toDto(model);

            return new ApiResponse<>(200, "DropDowns Fetched Successfully", dto);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(500, "An unexpected error occurred", null);
        }
    }


    public ApiResponse<TimeOffPolicyResponseDto> editPolicy(TimeOffPolicyEditRequestDto requestDto) {

        try {
            TimeOffPolicyEditRequestModel model = timeoffPolicyDtoMapper.toEditRequestModel(requestDto);
            timeOffPolicyService.editPolicy(model);

            return new ApiResponse<>(200, "Policy Updated Successfully", null);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(500, "An unexpected error occurred", null);
        }
    }

    public ApiResponse<String> assignPoliciesToUsers(TimeOffPolicyBulkAssignRequestDto dto) {
        try {
            TimeOffPolicyBulkAssignModel model = timeoffPolicyDtoMapper.toBulkAssignModel(dto);
            timeOffPolicyService.assignPolicies(model);

            return new ApiResponse<>(200, "Policies assigned successfully", null);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(500, "An unexpected error occurred", null);
        }
    }


    public ApiResponse<String> inactivatePolicy(String policyId, TimeOffPolicyInactivateRequestDto dto) {

        try {
            TimeOffPolicyInactivateModel model =
                    timeoffPolicyDtoMapper.toInactivateModel(dto);

            timeOffPolicyService.inactivatePolicy(policyId, model);

            String message = model.getActive()
                    ? "Policy activated successfully"
                    : "Policy deactivated successfully";

            return new ApiResponse<>(200, message, null);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(500, "An unexpected error occurred", null);
        }
    }


    public ApiResponse<List<TimeoffPoliciesDto>> getAllPolicies() {
        List<TimeoffPoliciesModel> models = timeOffPolicyService.getAllPolicies();
        List<TimeoffPoliciesDto> dtos =
                models.stream().map(timeoffPolicyDtoMapper::toDto)
                        .toList();
        return new ApiResponse<>(200, "Policies fetched successfully", dtos);
    }

    public ApiResponse<List<TimeoffPolicyDto>> getAllPolicy(){
        List<TimeoffPoliciesModel> model=timeOffPolicyService.getAllPolicy();
        List<TimeoffPolicyDto> dto=model.stream()
                .map(timeoffPolicyDtoMapper::toPolicyDto)
                .toList();
        return new ApiResponse<>(200,"policies fetched successfully",dto);
    }

    public ApiResponse<List<AccrualTypeEnumDto>> getAccrualStatus(){
        List<AccrualTypeEnumModel> model =timeOffPolicyService.getAccrualTypeStatus();
        List<AccrualTypeEnumDto> dto=model.stream().map(timeoffPolicyDtoMapper::toDto).toList();
        return new ApiResponse<>(200,"AccrualType fetched successfully",dto);
    }

    public ApiResponse<List<CompensationEnumDto>>getCompensation(){
        List<CompensationEnumModel> model=timeOffPolicyService.getCompensation();
             List<CompensationEnumDto> dto = model.stream().map(timeoffPolicyDtoMapper::toDto).toList();
              return new ApiResponse<>(200,"Compensation fetched successfully",dto);
    }

    public ApiResponse<TimeoffPoliciesDto>getPolicyById(String id){
        TimeoffPoliciesDto dto=timeoffPolicyDtoMapper.toDto(timeOffPolicyService.getPolicyById(id));
        return new ApiResponse<>(200,"policy fetched successfully",dto);
    }

    public ApiResponse<List<TimeoffPolicyDto>> getPolicyByUserId(String userId){
        List<TimeoffPoliciesModel> model=timeOffPolicyService.getPolicyByUserId(userId);
        List<TimeoffPolicyDto> dto = model.stream()
                .map(timeoffPolicyDtoMapper::toPolicyDto)
                .toList();
        return new ApiResponse<>(200, "Policies fetched successfully", dto);
    }

}
