package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.enums.IdGenerationType;
import java.util.List;

public interface IdGeneratorService {
    String generateNextId(IdGenerationType type);
    List<String> generateNextId(IdGenerationType type, int count);
}
