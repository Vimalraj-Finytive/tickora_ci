package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyResponseDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyRequestModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface TimeOffPolicyEntityMapper {
//    @Mappings({
//            @Mapping(target = "policyName", source = "policyName"),
//            @Mapping(target = "compensation", source = "compensation"),
//            @Mapping(target = "accrualType", source = "accrualType"),
//            @Mapping(target = "validityStartDate", source = "validityStartDate"),
//            @Mapping(target = "validityEndDate", source = "validityEndDate"),
//            @Mapping(target = "entitledType", source = "entitledType"),
//            @Mapping(target = "entitledUnits", source = "entitledUnits"),
//            @Mapping(target = "entitledHours", source = "entitledHours"),
//            @Mapping(target = "maxCarryForwardUnits", source = "maxCarryForwardUnits"),
//            @Mapping(target = "carryForward", source = "carryForward"),
//            @Mapping(target = "userPolicies", ignore = true)
//    })
    TimeoffPolicyEntity toEntity(TimeOffPolicyRequestDto dto);

    TimeoffPolicyEntity toEntity(TimeOffPolicyRequestModel model);

    TimeOffPolicyResponseModel toResponseModel(TimeoffPolicyEntity entity);

//    @Mappings({
//            @Mapping(target = "policyId", source = "policyId"),
//            @Mapping(target = "policyName", source = "policyName"),
//            @Mapping(target = "compensation", source = "compensation"),
//            @Mapping(target = "accrualType", source = "accrualType"),
//            @Mapping(target = "validityStartDate", source = "validityStartDate"),
//            @Mapping(target = "validityEndDate", source = "validityEndDate"),
//            @Mapping(target = "entitledType", source = "entitledType"),
//            @Mapping(target = "entitledUnits", source = "entitledUnits"),
//            @Mapping(target = "entitledHours", source = "entitledHours"),
//            @Mapping(target = "carryForward", source = "carryForward"),
//            @Mapping(target = "maxCarryForwardUnits", source = "maxCarryForwardUnits")
//    })
    TimeOffPolicyResponseDto toResponseDto(TimeoffPolicyEntity entity);
}
