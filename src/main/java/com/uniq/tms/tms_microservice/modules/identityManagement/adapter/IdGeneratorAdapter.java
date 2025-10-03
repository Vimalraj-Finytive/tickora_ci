package com.uniq.tms.tms_microservice.modules.identityManagement.adapter;

import com.uniq.tms.tms_microservice.modules.identityManagement.enums.IdGenerationTypeEnum;

public interface IdGeneratorAdapter {
    String findMaxIdByPrefix(IdGenerationTypeEnum type, String prefix);
}
