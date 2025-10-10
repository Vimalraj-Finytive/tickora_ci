package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Plan;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanDtoMapper {
    PlanDto toDto(Plan model);
    List<PlanDto> toDtoList(List<Plan> models);
}
