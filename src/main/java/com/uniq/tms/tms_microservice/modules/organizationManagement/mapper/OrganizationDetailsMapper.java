package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationDetailsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.OrganizationDetailsModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationDetailsMapper {


    @Mapping(target = "createdAt", expression = "java(map(entity.getCreatedAt()))")
    OrganizationDetailsModel toModel(OrganizationEntity entity);
    @Mapping(target = "createdAt", expression = "java(map(model.getCreatedAt()))")
    OrganizationEntity toEntity(OrganizationDetailsModel model);
    OrganizationDetailsDto toDto(OrganizationDetailsModel model);
    OrganizationDetailsModel toModel(OrganizationDetailsDto dto); List<OrganizationDetailsModel> toModel(List<OrganizationEntity> entities);
    List<OrganizationDetailsDto> toDto(List<OrganizationDetailsModel> models);

    default LocalDate map(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate() : null;
    }

    default LocalDateTime map(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }
}
