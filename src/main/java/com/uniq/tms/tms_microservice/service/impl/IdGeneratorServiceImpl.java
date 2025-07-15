package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.IdGeneratorAdapter;
import com.uniq.tms.tms_microservice.enums.IdGenerationType;
import com.uniq.tms.tms_microservice.repository.WorkScheduleRepository;
import com.uniq.tms.tms_microservice.service.IdGeneratorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class IdGeneratorServiceImpl implements IdGeneratorService {

    private static final Logger log = LogManager.getLogger(IdGeneratorServiceImpl.class);
    private final IdGeneratorAdapter idGeneratorAdapter;

    public IdGeneratorServiceImpl(IdGeneratorAdapter idGeneratorAdapter) {
        this.idGeneratorAdapter = idGeneratorAdapter;
    }

    @Override
    public String generateNextId(IdGenerationType type) {
        return generateNextId(type, 1).get(0);
    }

    @Override
    public List<String> generateNextId(IdGenerationType type, int count) {
        log.info("Generating {} IDs for type: {}", count, type);
        String prefix = type.getPrefix();
        String maxId = idGeneratorAdapter.findMaxIdByPrefix(type, prefix);
        log.info("Max ID: {}", maxId);

        int nextNumber = 1;
        if (maxId != null && maxId.startsWith(prefix)) {
            String numericPart = maxId.substring(prefix.length());
            try {
                nextNumber = Integer.parseInt(numericPart) + 1;
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid ID format: " + maxId);
            }
        }

        final int start = nextNumber;
        return IntStream.range(0, count)
                .mapToObj(i -> String.format("%s%03d", prefix, start + i))
                .collect(Collectors.toList());
    }
}
