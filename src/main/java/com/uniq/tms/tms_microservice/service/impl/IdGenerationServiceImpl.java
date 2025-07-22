package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.IdGeneratorAdapter;
import com.uniq.tms.tms_microservice.entity.OrgUserSequenceEntity;
import com.uniq.tms.tms_microservice.enums.IdGenerationType;
import com.uniq.tms.tms_microservice.repository.OrgUserSequenceRepository;
import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.service.IdGenerationService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class IdGenerationServiceImpl implements IdGenerationService {

    private static final Logger log = LogManager.getLogger(IdGenerationServiceImpl.class);

    private OrgUserSequenceRepository orgUserSequenceRepository;
    private final IdGeneratorAdapter idGeneratorAdapter;
    private final OrganizationRepository organizationRepository;

    public IdGenerationServiceImpl(OrgUserSequenceRepository orgUserSequenceRepository, IdGeneratorAdapter idGeneratorAdapter, OrganizationRepository organizationRepository) {
        this.orgUserSequenceRepository = orgUserSequenceRepository;
        this.idGeneratorAdapter = idGeneratorAdapter;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public String generateNextUserId(String orgId) {
        int updated = orgUserSequenceRepository.incrementSequence(orgId);

        if (updated == 0) {
            OrgUserSequenceEntity sequence = new OrgUserSequenceEntity();
            sequence.setOrgId(orgId);
            sequence.setLastNumber(1);
            orgUserSequenceRepository.save(sequence);
            return orgId.replaceAll("\\d","") + IdGenerationType.USER.getPrefix() + String.format("%05d", 1);
        }
        Integer latestNumber = orgUserSequenceRepository.getLastNumber(orgId);
        return orgId.replaceAll("\\d","") + IdGenerationType.USER.getPrefix() + String.format("%05d", latestNumber);
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

    @Override
    public String generateOrgPrefix(String orgName) {
        if (orgName == null || orgName.isEmpty()) return "XXXX";
        String[] words = orgName.trim().split("\\s+");
        String firstWord = words[0].toUpperCase();
        String primaryPrefix = firstWord.substring(0, Math.min(4, firstWord.length()));
        if (!organizationRepository.existsByOrganizationIdStartingWith("TK" + primaryPrefix)) {
            return primaryPrefix;
        }
        if (words.length > 1) {
            String fallbackPrefix = words[0].substring(0, 1).toUpperCase() +
                    words[1].substring(0, Math.min(3, words[1].length())).toUpperCase();
            if (!organizationRepository.existsByOrganizationIdStartingWith("TK" + fallbackPrefix)) {
                return fallbackPrefix;
            }
        }
        int counter = 1;
        String tempPrefix;
        do {
            tempPrefix = primaryPrefix + counter;
            counter++;
        } while (organizationRepository.existsByOrganizationIdStartingWith("TK" + tempPrefix));

        return tempPrefix;
    }
}
