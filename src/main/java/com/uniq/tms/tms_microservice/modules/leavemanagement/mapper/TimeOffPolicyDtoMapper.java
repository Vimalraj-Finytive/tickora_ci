package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import org.mapstruct.Mapper;
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

    TimeoffPoliciesDto toDto(TimeOffPoliciesModel model);

    TimeOffPoliciesModel toModel(TimeoffPoliciesDto dto);

    TimeoffPolicyDto toPolicyDto(TimeOffPoliciesModel model);

    AccrualTypeEnumDto toDto(AccrualTypeEnumModel model);

    AccrualTypeEnumModel toModel(AccrualTypeEnumDto dto);

    CompensationEnumModel toModel(CompensationEnumDto dto);

    CompensationEnumDto toDto(CompensationEnumModel model);

    List<TimeoffRequestResponseDto> toDtoList(List<TimeOffRequestResponseModel> models);
    RequestFilterModel toModel(RequestFilterDto dto);

}
