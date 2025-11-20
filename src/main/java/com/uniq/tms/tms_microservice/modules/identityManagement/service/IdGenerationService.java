package com.uniq.tms.tms_microservice.modules.identityManagement.service;

import com.uniq.tms.tms_microservice.modules.identityManagement.enums.IdGenerationTypeEnum;
import java.util.List;

public interface IdGenerationService {
    String generateNextUserId(String organizationId);
    String generateNextId(IdGenerationTypeEnum type);
    List<String> generateNextId(IdGenerationTypeEnum type, int count);
    String generateOrgPrefix(String orgName);
    String generateNextSecondaryUserId(String organizationId);
    String generateNextSubscriptionId(String organizationId);
    String generateNextPaymentID(String organizationId);
    String generatePayrollId(String organizationId);
    String generateNextTimeOffPolicyId();
}
