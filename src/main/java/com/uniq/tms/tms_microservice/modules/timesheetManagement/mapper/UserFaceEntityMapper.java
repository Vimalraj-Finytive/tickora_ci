package com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.UserEmbeddingDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.UserFaceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserFaceEntityMapper {

    @Mapping(target = "embeddings", expression = "java(mapEmbeddingToJson(body))")
    @Mapping(target = "userId", source = "userId")
    UserFaceEntity toEntity(UserEmbeddingDto body);

    default String mapEmbeddingToJson(UserEmbeddingDto body) {
        try {
            return new ObjectMapper().writeValueAsString(body.getEmbeddingLink());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert embedding to JSON", e);
        }
    }
}
