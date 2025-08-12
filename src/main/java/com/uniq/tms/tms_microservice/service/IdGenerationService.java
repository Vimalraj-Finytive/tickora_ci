package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.enums.IdGenerationType;
import java.util.List;

public interface IdGenerationService {
    String generateNextUserId(String organizationId);
    String generateNextId(IdGenerationType type);
    List<String> generateNextId(IdGenerationType type, int count);
    String generateOrgPrefix(String orgName);
    String generateNextSecondaryUserId(String organizationId);
}
