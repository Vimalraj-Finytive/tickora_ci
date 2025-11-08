package com.uniq.tms.tms_microservice.modules.identityManagement.service.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.adapter.IdGeneratorAdapter;
import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.identityManagement.entity.OrgUserSequenceEntity;
import com.uniq.tms.tms_microservice.modules.identityManagement.enums.IdGenerationTypeEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrgUserSequenceRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
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
            return orgId.replaceAll("\\d","") + IdGenerationTypeEnum.USER.getPrefix() + String.format("%05d", 1);
        }
        Integer latestNumber = orgUserSequenceRepository.getLastUserId(orgId);
        return orgId.replaceAll("\\d","") + IdGenerationTypeEnum.USER.getPrefix() + String.format("%05d", latestNumber);
    }

    @Override
    public String generateNextId(IdGenerationTypeEnum type) {
        return generateNextId(type, 1).getFirst();
    }

    @Override
    public List<String> generateNextId(IdGenerationTypeEnum type, int count) {
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

        String format = switch (type){
            case CALENDAR, CALENDAR_DETAILS, PUBLIC_HOLIDAY -> "%s%05d";
            default -> "%s%03d";
        };

        return IntStream.range(0, count)
                .mapToObj(i -> String.format(format, prefix, start + i))
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
    @Transactional
    public String generateNextSecondaryUserId(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("organizationId cannot be null or blank");
        }
        log.info("OrganizationId: {}", organizationId);
        int updated = orgUserSequenceRepository.incrementSecondaryUserSequence(organizationId);
        log.info("Increment number:{}", updated);
            if (updated == 0) {
                OrgUserSequenceEntity sequence = new OrgUserSequenceEntity();
                sequence.setOrgId(organizationId);
                sequence.setLastSecondaryUserId(1);
                orgUserSequenceRepository.save(sequence);
                return organizationId.replaceAll("\\d","") + IdGenerationTypeEnum.SECONDARY_USER.getPrefix() + String.format("%05d", 1);
            }
            log.info("Call repo to get last sequence");
            Integer latestNumber = orgUserSequenceRepository.getLastSecondaryUserId(organizationId);
        if (latestNumber == null) {
            orgUserSequenceRepository.updateLastSecondaryId(organizationId, 1);
            latestNumber = 1;
        }
            log.info("latestNumber:{}", latestNumber);
            return organizationId.replaceAll("\\d","") + IdGenerationTypeEnum.SECONDARY_USER.getPrefix() + String.format("%05d", latestNumber);
    }

    @Override
    public String generateNextSubscriptionId(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("organizationId cannot be null or blank");
        }
        log.info("OrganizationId: {}", organizationId);
        int updated = orgUserSequenceRepository.incrementSubscriptionSequence(organizationId);
        log.info("Increment number:{}", updated);
        if (updated == 0) {
            OrgUserSequenceEntity sequence = new OrgUserSequenceEntity();
            sequence.setOrgId(organizationId);
            sequence.setLastSubscriptionId(1);
            orgUserSequenceRepository.save(sequence);
            return organizationId.replaceAll("\\d","") + IdGenerationTypeEnum.SUBSCRIPTION.getPrefix() + String.format("%03d", 1);
        }
        log.info("Call repo to get last sequence of subscription Id");
        Integer latestNumber = orgUserSequenceRepository.getLastSubscription(organizationId);
        log.info("latestNumber:{}", latestNumber);
        if (latestNumber == null) {
            orgUserSequenceRepository.updateLastSubscriptionId(organizationId, 1);
            latestNumber = 1;
        }
        return organizationId.replaceAll("\\d","") + IdGenerationTypeEnum.SUBSCRIPTION.getPrefix() + String.format("%03d", latestNumber);
    }

    @Override
    public String generateNextPaymentID(String organizationId){
            if (organizationId == null || organizationId.isBlank()) {
                throw new IllegalArgumentException("organizationId cannot be null or blank");
            }
            log.info("Generating Payment ID for Organization: {}", organizationId);

            int updated = orgUserSequenceRepository.incrementPaymentSequence(organizationId);
            log.info("Increment result: {}", updated);


            if (updated == 0) {
                OrgUserSequenceEntity sequence = new OrgUserSequenceEntity();
                sequence.setOrgId(organizationId);
                sequence.setLastPaymentId(1);
                orgUserSequenceRepository.save(sequence);

                return organizationId.replaceAll("\\d", "")
                        + IdGenerationTypeEnum.PAYMENT.getPrefix()
                        + String.format("%04d", 1);
            }

            // Get latest number
            Integer latestNumber = orgUserSequenceRepository.getLastPaymentId(organizationId);
            if (latestNumber == null) {
                orgUserSequenceRepository.updateLastPaymentId(organizationId, 1);
                latestNumber = 1;
            }

            return organizationId.replaceAll("\\d", "")
                    + IdGenerationTypeEnum.PAYMENT.getPrefix()
                    + String.format("%04d", latestNumber);
        }
}
