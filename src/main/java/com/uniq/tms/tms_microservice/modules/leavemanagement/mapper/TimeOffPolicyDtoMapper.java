package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimeOffPolicyDtoMapper {

    TimeOffPolicyRequestModel toRequestModel(TimeOffPolicyRequestDto dto);

    TimeOffPolicyEditRequestModel toEditRequestModel(TimeOffPolicyEditRequestDto dto);

    TimeOffPolicyResponseDto toResponseDto(TimeOffPolicyResponseModel model);

    EntitledTypeDropdownDto toDto(EntitledTypeDropdownModel model);

    TimeOffPolicyBulkAssignModel toBulkAssignModel(TimeOffPolicyBulkAssignRequestDto dto);

    TimeOffPolicyInactivateModel toInactivateModel(TimeOffPolicyInactivateRequestDto dto);

    TimeOffRequest toRequestModel(TimeOffRequestDto dto);

    EmployeeStatusUpdate toStatusModel(EmployeeStatusUpdateDto dto);

    AdminStatusUpdate toAdminStatusModel(AdminStatusUpdateDto dto);

    TimeoffPoliciesDto toDto(TimeoffPoliciesModel model);

    TimeoffPoliciesModel toModel(TimeoffPoliciesDto dto);

    TimeoffPolicyDto toPolicyDto(TimeoffPoliciesModel model);

    AccrualTypeEnumDto toDto(AccrualTypeEnumModel model);

    AccrualTypeEnumModel toModel(AccrualTypeEnumDto dto);

    CompensationEnumModel toModel(CompensationEnumDto dto);

    CompensationEnumDto toDto(CompensationEnumModel model);

//    LeaveBalanceDto toDto(LeaveBalanceModel model);
//    LeaveBalanceModel toModel(LeaveBalanceDto dto);
//    List<LeaveBalanceDto> toDtoList(List<LeaveBalanceModel> model);

    List<TimeoffRequestResponseDto> toDtoList(List<TimeoffRequestResponseModel> models);
    RequestFilterModel toModel(RequestFilterDto dto);



}
