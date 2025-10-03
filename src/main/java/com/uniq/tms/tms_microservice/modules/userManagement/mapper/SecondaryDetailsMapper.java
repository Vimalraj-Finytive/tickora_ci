package com.uniq.tms.tms_microservice.modules.userManagement.mapper;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.SecondaryDetailsDto;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.SecondaryDetailsEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.model.SecondaryDetails;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SecondaryDetailsMapper {
    SecondaryDetailsEntity toEntity(SecondaryDetails model);
    SecondaryDetails toMiddleware(SecondaryDetailsDto dto);
    SecondaryDetailsDto toMiddleware(SecondaryDetailsEntity secondaryDetails);
}
