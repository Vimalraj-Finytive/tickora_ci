package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.OrgSetupValidationResponse;
import com.uniq.tms.tms_microservice.dto.UserRole;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.Organization;
import com.uniq.tms.tms_microservice.model.OrganizationType;
import com.uniq.tms.tms_microservice.model.User;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.service.IdGenerationService;
import com.uniq.tms.tms_microservice.service.OrganizationService;
import com.uniq.tms.tms_microservice.util.EmailUtil;
import com.uniq.tms.tms_microservice.util.PasswordUtil;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static com.uniq.tms.tms_microservice.util.TextUtil.isBlank;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LogManager.getLogger(OrganizationServiceImpl.class);
    private final UserEntityMapper userEntityMapper;
    private final UserAdapter userAdapter;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final EmailUtil emailUtil;
    private final RoleRepository roleRepository;
    private final IdGenerationService idGenerationService;

    public OrganizationServiceImpl(UserEntityMapper userEntityMapper, UserAdapter userAdapter, WorkScheduleAdapter workScheduleAdapter, EmailUtil emailUtil, RoleRepository roleRepository, IdGenerationService idGenerationService) {
        this.userEntityMapper = userEntityMapper;
        this.userAdapter = userAdapter;
        this.workScheduleAdapter = workScheduleAdapter;
        this.emailUtil = emailUtil;
        this.roleRepository = roleRepository;
        this.idGenerationService = idGenerationService;
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
        String prefix = idGenerationService.generateOrgPrefix(organization.getOrgName()).toUpperCase();
        log.info("Prefix: {}", prefix);

        // Generate formatted organization number
        Long count = userAdapter.countOrganizations();
        String formattedNumber = String.format("%04d", count + 1);

        // Final orgId
        String orgId = prefix + formattedNumber;
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

    /**
     *
     * @param organization
     * @param organizationId
     * create superadmin along with organization creation
     * @return success once SA is created
     */
    @Transactional
    public ApiResponse createSuperAdminUser(Organization organization, String organizationId) {
        if (isBlank(organization.getUserName()) || isBlank(organization.getMobile()) || isBlank(organization.getEmail())) {
            throw new CommonExceptionHandler.BadRequestException("Mandatory fields must not be null");
        }
        userAdapter.findByMobileNumber(organization.getMobile()).ifPresent(user -> {
            if (!user.isActive()) throw new CommonExceptionHandler.DuplicateUserException("Inactive user.");
            throw new CommonExceptionHandler.DuplicateUserException("Mobile number already in use.");
        });

        userAdapter.findByEmail(organization.getEmail()).ifPresent(user -> {
            if (!user.isActive()) throw new CommonExceptionHandler.DuplicateUserException("Inactive user.");
            throw new CommonExceptionHandler.DuplicateUserException("Email already in use.");
        });

        Long roleId = Long.valueOf(UserRole.SUPERADMIN.getHierarchyLevel());
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        UserEntity entity = new UserEntity();
        entity.setUserName(organization.getUserName());
        entity.setEmail(organization.getEmail());
        entity.setMobileNumber(organization.getMobile());
        entity.setOrganizationId(organizationId);
        String customUserId = idGenerationService.generateNextUserId(organizationId);
        log.info("SuperAdmin User Id:{}",customUserId);
        entity.setUserId(customUserId);
        entity.setRole(role);
        entity.setDateOfJoining(LocalDate.now());
        String defaultPassword = PasswordUtil.generateDefaultPassword();
        String encryptedPassword = PasswordUtil.encryptPassword(defaultPassword);
        entity.setPassword(encryptedPassword);
        entity.setDefaultPassword(true);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setWorkSchedule(null);
        UserEntity savedUser = userAdapter.saveUser(entity);

        emailUtil.sendAccountCreationEmail(
                savedUser.getEmail(), savedUser.getUserName(), defaultPassword, true
        );

        User finalUser = userEntityMapper.toMiddleware(savedUser);
        return new ApiResponse(201, "SuperAdmin created successfully", finalUser);
    }

    /**
     * Validate the Organization name
     * @param organization
     */
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

    /**
     *
     * @return List of organization type
     */
    @Override
    public List<OrganizationTypeEntity> getOrgType() {
        return userAdapter.getAllOrgType();
    }

    /**
     *
     * @param orgId
     * Validate Location and WorkSchedule of the organization
     * @return boolean based on location & WS
     */
    @Override
    public OrgSetupValidationResponse getValidation(String orgId) {
        List<LocationEntity> locationEntities = userAdapter.findLocation(orgId);
        List<WorkScheduleEntity> workScheduleEntities = workScheduleAdapter.findAllScheduleById(orgId);
        boolean hasLocations = locationEntities != null && !locationEntities.isEmpty();
        boolean hasSchedules = workScheduleEntities != null && !workScheduleEntities.isEmpty();
        return new OrgSetupValidationResponse(hasLocations, hasSchedules);
    }

    @Override
    public OrganizationType getUserOrgType(String orgId) {
        OrganizationEntity org = userAdapter.findByOrgId(orgId)
                .orElseThrow(() -> new RuntimeException("No organization found"));

        if (org.getOrgType() == null) {
            throw new RuntimeException("Organization type not found");
        }

        OrganizationTypeEntity orgTypeEntity = userAdapter.findOrgType(org.getOrgType());
        OrganizationType dto = userEntityMapper.toModel(orgTypeEntity);
        if ("Academic".equalsIgnoreCase(orgTypeEntity.getOrgTypeName())) {
            dto.setShowFilter(true);
        } else {
            dto.setShowFilter(false);
        }

        return dto;
    }

}
