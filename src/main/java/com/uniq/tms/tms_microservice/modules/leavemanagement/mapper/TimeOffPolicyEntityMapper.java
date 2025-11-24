package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyResponseDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface TimeOffPolicyEntityMapper {

    TimeOffPolicyEntity toEntity(TimeOffPolicyRequestDto dto);

    TimeOffPolicyEntity toEntity(TimeOffPolicyRequestModel model);

    TimeOffPolicyResponseModel toResponseModel(TimeOffPolicyEntity entity);

    TimeOffPolicyResponseDto toResponseDto(TimeOffPolicyEntity entity);

    TimeOffPoliciesModel toModel(TimeOffPolicyEntity entity);

    TimeOffPolicyEntity toEntity(TimeOffPoliciesModel model);

    List<TimeOffPoliciesModel> toModelList(List<TimeOffPolicyEntity> entity);

    List<TimeOffRequestResponseModel> toModel(List<TimeOffRequestEntity> entityList);

    @Mapping(source = "policy.policyName", target = "policyName")
    TimeOffRequestResponseModel toModel(TimeoffRequestEntity entity);

    List<TimeOffRequestResponseModel> toResponseModelList(List<TimeOffRequestEntity> entities);
    @Mapping(source = "policy.policyName", target = "policyName")
    LeaveBalanceModel toModel(LeaveBalanceEntity entity);
    List<LeaveBalanceModel> toBalanceModelList(List<LeaveBalanceEntity> entities);


}
