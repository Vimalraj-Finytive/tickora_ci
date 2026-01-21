package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.shared.dto.EnumDto;
import com.uniq.tms.tms_microservice.shared.dto.EnumModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface TimeOffPolicyDtoMapper {

    TimeOffPolicyRequestModel toRequestModel(TimeOffPolicyRequestDto dto);

    TimeOffPolicyEditRequestModel toEditRequestModel(TimeOffPolicyEditRequestDto dto);

    TimeOffPolicyResponseDto toResponseDto(TimeOffPolicyResponseModel model);

    TimeOffPolicyBulkAssignModel toBulkAssignModel(TimeOffPolicyBulkAssignRequestDto dto);

    TimeOffPolicyInactivateModel toInactivateModel(TimeOffPolicyInactivateRequestDto dto);

    TimeOffRequest toRequestModel(TimeOffRequestDto dto);

    EmployeeStatusUpdate toStatusModel(EmployeeStatusUpdateDto dto);

    AdminStatusUpdate toAdminStatusModel(AdminStatusUpdateDto dto);

    TimeoffPoliciesDto toDto(TimeOffPoliciesModel model);

    TimeOffPoliciesModel toModel(TimeoffPoliciesDto dto);

    TimeoffPolicyDto toPolicyDto(TimeOffPoliciesModel model);

    LeaveBalanceDto toDto(LeaveBalanceModel model);

    LeaveBalanceModel toModel(LeaveBalanceDto dto);

    List<LeaveBalanceDto> toDtoLeaveList(List<LeaveBalanceModel> model);

    List<TimeoffRequestResponseDto> toDtoList(List<TimeOffRequestResponseModel> models);

      EnumDto toDto (EnumModel model);

      EnumModel toModel(EnumDto dto);

    TimeOffRequestGroupDto toDto(TimeOffRequestGroupModel model) ;

    List<TimeOffRequestGroupDto> toGroupDtoList(List<TimeOffRequestGroupModel> model);

    Map<String, List<TimeOffRequestGroupDto>> toDtoList(Map<String, List<TimeOffRequestGroupModel>> model);

    TimeOffExportRequest toModel(TimeOffExportRequestDto dto);

    ViewerDto toDto(ViewerModel model);

    List<ViewerDto> toViewerDtoList(List<ViewerModel> model);

    TimeOffExportDto toDto(TimeOffExportModel model);

    List<TimeOffExportDto> toDtoLists(List<TimeOffExportModel> model);

    List<EditUserPolicyModel> toModel(List<EditUserPolicyDto> editUserPolicyDto);

    TimeOffPolicyTemplateDto toDto(TimeOffPolicyTemplateModel timeOffPolicyTemplateModel);
}
