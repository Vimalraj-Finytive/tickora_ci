package com.uniq.tms.tms_microservice.adapter;

import com.uniq.tms.tms_microservice.enums.IdGenerationType;

public interface IdGeneratorAdapter {
    String findMaxIdByPrefix(IdGenerationType type, String prefix);
}
