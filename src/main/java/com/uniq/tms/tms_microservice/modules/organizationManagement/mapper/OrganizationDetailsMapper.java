package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationDetailsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.OrganizationDetailsModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationDetailsMapper {

    OrganizationDetailsModel toModel(OrganizationEntity entity);

    OrganizationEntity toEntity(OrganizationDetailsModel model);

    OrganizationDetailsDto toDto(OrganizationDetailsModel model);
    OrganizationDetailsModel toModel(OrganizationDetailsDto dto);

    SubscriptionDto toSubscriptionSummary(SubscriptionDto activePlan);
}
