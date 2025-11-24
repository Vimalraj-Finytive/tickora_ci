package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyResponseDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPoliciesModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyRequestModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyResponseModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
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
    @Mapping(source = "status", target = "status")
    TimeOffRequestResponseModel toModel(TimeOffRequestEntity entity);

    List<TimeOffRequestResponseModel> toResponseModelList(List<TimeOffRequestEntity> entities);

}
