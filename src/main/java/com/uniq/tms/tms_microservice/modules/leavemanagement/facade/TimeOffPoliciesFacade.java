package com.uniq.tms.tms_microservice.modules.leavemanagement.facade;

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
