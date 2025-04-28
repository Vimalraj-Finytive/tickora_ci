package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.SecondaryDetailsDto;
import com.uniq.tms.tms_microservice.entity.SecondaryDetailsEntity;
import com.uniq.tms.tms_microservice.model.SecondaryDetails;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SecondaryDetailsMapper {
    SecondaryDetailsEntity toEntity(SecondaryDetails model);
    SecondaryDetails toMiddleware(SecondaryDetailsDto dto);

}
