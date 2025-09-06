package com.uniq.tms.tms_microservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.dto.UserEmbeddingDto;
import com.uniq.tms.tms_microservice.entity.UserFaceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
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
