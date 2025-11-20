package com.uniq.tms.tms_microservice.modules.leavemanagement.facade;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AccrualTypeEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CompensationEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPoliciesDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPolicyDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeoffPolicyDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AccrualTypeEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CompensationEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffPolicyService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class TimeOffPoliciesFacade {

    private final TimeOffPolicyService timeOffPolicyService;
    private final TimeOffPolicyDtoMapper timeOffPolicyDtoMapper;

    public TimeOffPoliciesFacade(TimeOffPolicyService timeOffPolicyService, TimeOffPolicyDtoMapper timeOffPolicyDtoMapper) {
        this.timeOffPolicyService = timeOffPolicyService;
        this.timeOffPolicyDtoMapper = timeOffPolicyDtoMapper;
    }


    public ApiResponse<TimeOffPolicyResponseDto> createPolicy(TimeOffPolicyRequestDto requestDto) {

        try {
            TimeOffPolicyRequestModel requestModel = timeOffPolicyDtoMapper.toRequestModel(requestDto);
            TimeOffPolicyResponseModel responseModel = timeOffPolicyService.createPolicy(requestModel);
            TimeOffPolicyResponseDto responseDto = timeOffPolicyDtoMapper.toResponseDto(responseModel);

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
            EntitledTypeDropdownDto dto = timeOffPolicyDtoMapper.toDto(model);

            return new ApiResponse<>(200, "DropDowns Fetched Successfully", dto);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(500, "An unexpected error occurred", null);
        }
    }


    public ApiResponse<TimeOffPolicyResponseDto> editPolicy(TimeOffPolicyEditRequestDto requestDto) {

        try {
            TimeOffPolicyEditRequestModel model = timeOffPolicyDtoMapper.toEditRequestModel(requestDto);
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
            TimeOffPolicyBulkAssignModel model = timeOffPolicyDtoMapper.toBulkAssignModel(dto);
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
                    timeOffPolicyDtoMapper.toInactivateModel(dto);

            timeOffPolicyService.inactivatePolicy(policyId, model);

            String message = model.getStatus()
                    ? "Policy activated successfully"
                    : "Policy deactivated successfully";

            return new ApiResponse<>(200, message, null);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(500, "An unexpected error occurred", null);
        }
    }

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimeOffPoliciesFacade {

    private final TimeOffPolicyService service;
    private final TimeoffPolicyDtoMapper mapper;

    public TimeOffPoliciesFacade(TimeOffPolicyService service, TimeoffPolicyDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public ApiResponse<List<TimeoffPoliciesDto>> getAllPolicies() {
        List<TimeoffPoliciesModel> models = service.getAllPolicies();
        List<TimeoffPoliciesDto> dtos =
                models.stream().map(mapper::toDto)
                        .toList();
        return new ApiResponse<>(200, "Policies fetched successfully", dtos);
    }

    public ApiResponse<List<TimeoffPolicyDto>> getAllPolicy(){
        List<TimeoffPoliciesModel> model=service.getAllPolicy();
        List<TimeoffPolicyDto> dto=model.stream()
                .map(mapper::toPolicyDto)
                .toList();
        return new ApiResponse<>(200,"policies fetched successfully",dto);
    }

    public ApiResponse<List<AccrualTypeEnumDto>> getAccrualStatus(){
        List<AccrualTypeEnumModel> model =service.getAccrualTypeStatus();
        List<AccrualTypeEnumDto> dto=model.stream().map(mapper::toDto).toList();
        return new ApiResponse<>(200,"AccrualType fetched successfully",dto);
    }

    public ApiResponse<List<CompensationEnumDto>>getCompensation(){
        List<CompensationEnumModel> model=service.getCompensation();
             List<CompensationEnumDto> dto = model.stream().map(mapper::toDto).toList();
              return new ApiResponse<>(200,"Compensation fetched successfully",dto);
    }

    public ApiResponse<TimeoffPoliciesDto>getPolicyById(String id){
        TimeoffPoliciesDto dto=mapper.toDto(service.getPolicyById(id));
        return new ApiResponse<>(200,"policy fetched successfully",dto);
    }

    public ApiResponse<List<TimeoffPolicyDto>> getPolicyByUserId(String userId){
        List<TimeoffPoliciesModel> model=service.getPolicyByUserId(userId);
        List<TimeoffPolicyDto> dto = model.stream()
                .map(mapper::toPolicyDto)
                .toList();
        return new ApiResponse<>(200, "Policies fetched successfully", dto);
    }

}
