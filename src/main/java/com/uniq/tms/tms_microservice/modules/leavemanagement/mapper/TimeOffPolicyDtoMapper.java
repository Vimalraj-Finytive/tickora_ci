package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,unmappedSourcePolicy = ReportingPolicy.IGNORE)
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

    LeaveBalanceDto toDto(LeaveBalanceModel model);

    LeaveBalanceModel toModel(LeaveBalanceDto dto);

    List<LeaveBalanceDto> toDtoLeaveList(List<LeaveBalanceModel> model);

    List<TimeoffRequestResponseDto> toDtoList(List<TimeOffRequestResponseModel> models);

    StatusEnumDto toDto (StatusEnumModel model);

    StatusEnumModel toModel(StatusEnumDto dto);

}
