package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.enums.IdGenerationTypeEnum;
import java.util.List;

public interface IdGenerationService {
    String generateNextUserId(String organizationId);
    String generateNextId(IdGenerationTypeEnum type);
    List<String> generateNextId(IdGenerationTypeEnum type, int count);
    String generateOrgPrefix(String orgName);
    String generateNextSecondaryUserId(String organizationId);
}
