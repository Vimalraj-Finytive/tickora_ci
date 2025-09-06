package com.uniq.tms.tms_microservice.adapter;

import com.uniq.tms.tms_microservice.enums.IdGenerationTypeEnum;

public interface IdGeneratorAdapter {
    String findMaxIdByPrefix(IdGenerationTypeEnum type, String prefix);
}
