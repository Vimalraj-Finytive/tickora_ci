package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AccrualTypeEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CompensationEnumDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPoliciesDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPolicyDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AccrualTypeEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CompensationEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimeoffPolicyDtoMapper {

    TimeoffPoliciesDto toDto(TimeoffPoliciesModel model);
    TimeoffPoliciesModel toModel(TimeoffPoliciesDto dto);
    TimeoffPolicyDto toPolicyDto(TimeoffPoliciesModel model);
    AccrualTypeEnumDto toDto(AccrualTypeEnumModel model);
    AccrualTypeEnumModel toModel(AccrualTypeEnumDto dto);
    CompensationEnumModel toModel(CompensationEnumDto dto);
    CompensationEnumDto toDto(CompensationEnumModel model);

}
