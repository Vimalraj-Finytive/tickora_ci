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
            sequence.setLastUserId(1);
            orgUserSequenceRepository.save(sequence);
            return orgId.replaceAll("\\d","") + IdGenerationType.USER.getPrefix() + String.format("%05d", 1);
        }
        Integer latestNumber = orgUserSequenceRepository.getLastUserId(orgId);
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
        if (orgName == null || orgName.trim().isEmpty()) return "XXXX";
        String[] words = orgName.trim().split("\\s+");
        String firstWord = words[0].toUpperCase();
        if (!organizationRepository.existsByOrganizationIdStartingWith(firstWord)) {
            return firstWord;
        }
        if (words.length > 1) {
            String secondWord = words[1].toUpperCase();
            for (int i = 1; i <= secondWord.length(); i++) {
                String combinedPrefix = firstWord + secondWord.substring(0, i);
                if (!organizationRepository.existsByOrganizationIdStartingWith(combinedPrefix)) {
                    return combinedPrefix;
                }
            }
        }
        int suffixIndex = 0;
        String tempPrefix;
        do {
            String alphaSuffix = toAlphabeticSuffix(suffixIndex);
            tempPrefix = firstWord + alphaSuffix;
            suffixIndex++;
        } while (organizationRepository.existsByOrganizationIdStartingWith(tempPrefix));

        return tempPrefix;
    }
    private String toAlphabeticSuffix(int index) {
        index += 23;
        StringBuilder suffix = new StringBuilder();
        while (index >= 0) {
            suffix.insert(0, (char) ('A' + (index % 26)));
            index = index / 26 - 1;
        }
        return suffix.toString();
    }

    @Override
    public String generateNextSecondaryUserId(String organizationId) {
            int updated = orgUserSequenceRepository.incrementSecondaryUserSequence(organizationId);
            if (updated == 0) {
                OrgUserSequenceEntity sequence = new OrgUserSequenceEntity();
                sequence.setOrgId(organizationId);
                sequence.setLastSecondaryUserId(1);
                orgUserSequenceRepository.save(sequence);
                return organizationId.replaceAll("\\d","") + IdGenerationType.SECONDARY_USER.getPrefix() + String.format("%05d", 1);
            }
            Integer latestNumber = orgUserSequenceRepository.getLastSecondaryUserId(organizationId);
            return organizationId.replaceAll("\\d","") + IdGenerationType.SECONDARY_USER.getPrefix() + String.format("%05d", latestNumber);
    }
}
