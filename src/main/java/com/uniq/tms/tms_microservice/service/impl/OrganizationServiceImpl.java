package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.OrgSetupValidationResponse;
import com.uniq.tms.tms_microservice.dto.UserRole;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.enums.IdGenerationType;
import com.uniq.tms.tms_microservice.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.Organization;
import com.uniq.tms.tms_microservice.model.User;
import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.service.OrganizationService;
import com.uniq.tms.tms_microservice.util.EmailUtil;
import com.uniq.tms.tms_microservice.util.PasswordUtil;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static com.uniq.tms.tms_microservice.util.TextUtil.isBlank;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LogManager.getLogger(OrganizationServiceImpl.class);
    private final UserEntityMapper userEntityMapper;
    private final UserAdapter userAdapter;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final OrganizationRepository organizationRepository;
    private final EmailUtil emailUtil;
    private final RoleRepository roleRepository;

    public OrganizationServiceImpl(UserEntityMapper userEntityMapper, UserAdapter userAdapter, WorkScheduleAdapter workScheduleAdapter, OrganizationRepository organizationRepository, EmailUtil emailUtil, RoleRepository roleRepository) {
        this.userEntityMapper = userEntityMapper;
        this.userAdapter = userAdapter;
        this.workScheduleAdapter = workScheduleAdapter;
        this.organizationRepository = organizationRepository;
        this.emailUtil = emailUtil;
        this.roleRepository = roleRepository;
    }

    /**
     *
     * @param organization
     * @Create organization and Org Superadmin
     * @return Success
     */
    @Override
    @Transactional
    public Organization create(Organization organization) {
        OrganizationEntity entity = userEntityMapper.toEntity(organization);

        // Generate unique org prefix
        String prefix = generateOrgPrefix(organization.getOrgName()).toUpperCase();
        log.info("Prefix: {}", prefix);

        // Generate formatted organization number
        Long count = userAdapter.countOrganizations();
        String formattedNumber = String.format("%04d", count + 1);

        // Final orgId
        String orgId = IdGenerationType.TICKORA.getPrefix() + prefix + formattedNumber;
        log.info("Organization Id : {}", orgId);

        entity.setOrganizationId(orgId);

        try {
            if (userAdapter.existsByOrganizationId(orgId)) {
                throw new RuntimeException("Organization ID already exists: " + orgId);
            }

            OrganizationEntity response = userAdapter.create(entity);
            log.info("Organization Created Successfully");

            Organization orgModel = userEntityMapper.toModel(response);

            log.info("Creating Superadmin based on created organization");
            createSuperAdminUser(organization, orgId);

            return orgModel;

        } catch (Exception e) {
            log.error("Organization creation failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String generateOrgPrefix(String orgName) {
        if (orgName == null || orgName.isEmpty()) return "XXXX";

        String[] words = orgName.trim().split("\\s+");

        // Case 1: Try first word (up to 4 letters)
        String firstWord = words[0].toUpperCase();
        String primaryPrefix = firstWord.substring(0, Math.min(4, firstWord.length()));

        if (!organizationRepository.existsByOrganizationIdStartingWith("TK" + primaryPrefix)) {
            return primaryPrefix;
        }

        // Case 2: Fallback: first letter from first + 3 letters from second word
        if (words.length > 1) {
            String fallbackPrefix = words[0].substring(0, 1).toUpperCase() +
                    words[1].substring(0, Math.min(3, words[1].length())).toUpperCase();
            if (!organizationRepository.existsByOrganizationIdStartingWith("TK" + fallbackPrefix)) {
                return fallbackPrefix;
            }
        }

        // Case 3: Final fallback with numeric suffix
        int counter = 1;
        String tempPrefix;
        do {
            tempPrefix = primaryPrefix + counter;
            counter++;
        } while (organizationRepository.existsByOrganizationIdStartingWith("TK" + tempPrefix));

        return tempPrefix;
    }

    @Transactional
    public ApiResponse createSuperAdminUser(Organization organization, String organizationId) {
        // Step 1: Validate mandatory fields
        if (isBlank(organization.getUserName()) || isBlank(organization.getMobile()) || isBlank(organization.getEmail())) {
            throw new CommonExceptionHandler.BadRequestException("Mandatory fields must not be null");
        }

        // Step 2: Validate duplicates
        userAdapter.findByMobileNumber(organization.getMobile()).ifPresent(user -> {
            if (!user.isActive()) throw new CommonExceptionHandler.DuplicateUserException("Inactive user.");
            throw new CommonExceptionHandler.DuplicateUserException("Mobile number already in use.");
        });

        userAdapter.findByEmail(organization.getEmail()).ifPresent(user -> {
            if (!user.isActive()) throw new CommonExceptionHandler.DuplicateUserException("Inactive user.");
            throw new CommonExceptionHandler.DuplicateUserException("Email already in use.");
        });

        // Step 3: Assign SuperAdmin role
        Long roleId = Long.valueOf(UserRole.SUPERADMIN.getHierarchyLevel());
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Step 4: Create UserEntity
        UserEntity entity = new UserEntity();
        entity.setUserName(organization.getUserName());
        entity.setEmail(organization.getEmail());
        entity.setMobileNumber(organization.getMobile());
        entity.setOrganizationId(organizationId);
        entity.setRole(role);
        entity.setDateOfJoining(LocalDate.now());
        // Step 5: Set password
        String defaultPassword = PasswordUtil.generateDefaultPassword();
        String encryptedPassword = PasswordUtil.encryptPassword(defaultPassword);
        entity.setPassword(encryptedPassword);
        entity.setDefaultPassword(true);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setWorkSchedule(null);
        // Step 6: Save and respond
        UserEntity savedUser = userAdapter.saveUser(entity);

        // Send email
        emailUtil.sendAccountCreationEmail(
                savedUser.getEmail(), savedUser.getUserName(), defaultPassword, true
        );

        User finalUser = userEntityMapper.toMiddleware(savedUser);
        return new ApiResponse(201, "SuperAdmin created successfully", finalUser);
    }

    @Override
    public Organization validate(Organization organization) {
        OrganizationEntity organizationEntity = userEntityMapper.toEntity(organization);
        OrganizationEntity response = userAdapter.findByOrgName(organizationEntity.getOrgName());
        if(response != null) {
            if (organization.getOrgName().equalsIgnoreCase(response.getOrgName())) {
                throw new RuntimeException("Name already Exists under other organization");
            }
        }
        return userEntityMapper.toModel(response);
    }

    @Override
    public List<OrganizationTypeEntity> getOrgType() {
        return userAdapter.getAllOrgType();
    }

    @Override
    public OrgSetupValidationResponse getValidation(String orgId) {
        List<LocationEntity> locationEntities = userAdapter.findLocation(orgId);
        List<WorkScheduleEntity> workScheduleEntities = workScheduleAdapter.findAllScheduleById(orgId);
        boolean hasLocations = locationEntities != null && !locationEntities.isEmpty();
        boolean hasSchedules = workScheduleEntities != null && !workScheduleEntities.isEmpty();
        return new OrgSetupValidationResponse(hasLocations, hasSchedules);
    }
}
