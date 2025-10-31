package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.adapter.LocationAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.OrganizationDetailsMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.OrganizationEntityMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationTypeRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheKeyConfig;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.shared.util.CacheEventPublisherUtil;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.RoleName;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.shared.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.CountryEnum;
import com.uniq.tms.tms_microservice.shared.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.shared.helper.ExceptionHelper;
import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationService;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserSchemaMappingEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserService;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LogManager.getLogger(OrganizationServiceImpl.class);
    private final OrganizationEntityMapper organizationEntityMapper;
    private final UserAdapter userAdapter;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final IdGenerationService idGenerationService;
    private final DataSource dataSource;
    private final ExceptionHelper exceptionHelper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationTypeRepository organizationTypeRepository;
    private final UserService userService;
    private final OrganizationAdapter organizationAdapter;
    private final CacheKeyConfig cacheKeyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;
    private final ApplicationEventPublisher publisher;
    private final LocationAdapter locationAdapter;
    private final SubscriptionService subscriptionService;
    private final OrganizationDetailsMapper mapper;
    private final SubscriptionAdapter subscriptionAdapter;

    public OrganizationServiceImpl(OrganizationEntityMapper organizationEntityMapper, UserAdapter userAdapter, WorkScheduleAdapter workScheduleAdapter,
                                   IdGenerationService idGenerationService, DataSource dataSource, ExceptionHelper exceptionHelper,
                                   OrganizationRepository organizationRepository, OrganizationTypeRepository organizationTypeRepository,
                                   UserService userService, OrganizationAdapter organizationAdapter, CacheKeyConfig cacheKeyConfig,
                                   CacheReloadHandlerRegistry cacheReloadHandlerRegistry, ApplicationEventPublisher publisher,
                                   LocationAdapter locationAdapter, SubscriptionService subscriptionService, OrganizationDetailsMapper mapper, SubscriptionAdapter subscriptionAdapter) {
        this.organizationEntityMapper = organizationEntityMapper;
        this.userAdapter = userAdapter;
        this.workScheduleAdapter = workScheduleAdapter;
        this.idGenerationService = idGenerationService;
        this.dataSource = dataSource;
        this.exceptionHelper = exceptionHelper;
        this.organizationRepository = organizationRepository;
        this.organizationTypeRepository = organizationTypeRepository;
        this.userService = userService;
        this.organizationAdapter = organizationAdapter;
        this.cacheKeyConfig = cacheKeyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
        this.publisher = publisher;
        this.locationAdapter = locationAdapter;
        this.subscriptionService = subscriptionService;
        this.mapper = mapper;

        this.subscriptionAdapter = subscriptionAdapter;
    }

    @Value("${cache.redis.enabled}")
    private boolean isRedisEnabled;

    /**
     * @param organization
     * @return SuccessT
     * @Create organization and Org Superadmin
     */
    @Override
    @Transactional
    public Organization create(Organization organization) {
        String schemaName = organization.getOrgName().toLowerCase().replaceAll("\\s+", "_");
        log.info("Testing");
        OrganizationEntity entity = organizationEntityMapper.toEntity(organization);

        // Generate orgId
        String prefix = idGenerationService.generateOrgPrefix(organization.getOrgName()).toUpperCase();
        Long count = organizationAdapter.countOrganizations();
        String formattedNumber = String.format("%04d", count);
        String orgId = prefix + formattedNumber;

        entity.setOrganizationId(orgId);
        entity.setTimeZone(CountryEnum.getTimeZoneByCountry(organization.getCountry()));
        log.info("Getting Org Range from Enum:{}", organization.getOrgSize());
        log.info("orgRange:{}", entity.getOrgSize());
        entity.setSchemaName(schemaName);

        if (organizationAdapter.existsByOrganizationId(orgId)) {
            throw new RuntimeException("Organization ID already exists: " + orgId);
        }

        try {
            OrganizationEntity savedOrg = saveOrganizationPublic(entity);
            Organization orgModel = organizationEntityMapper.toModel(savedOrg);

            createSchema(schemaName);
            initSchemaTables(schemaName, organization.getOrgType());

            TenantContext.setCurrentTenant(schemaName);

            ApiResponse response = userService.createSuperAdminUser(organization, orgId, schemaName);

            TenantContext.setCurrentTenant("public");

            log.info("setting user map:{}", orgId);
            UserSchemaMappingEntity mapping = organizationEntityMapper.toSchema(
                    organization.getEmail(),
                    organization.getMobile(),
                    orgId,
                    schemaName
            );

            try {
                userAdapter.create(mapping);
            } catch (org.hibernate.exception.ConstraintViolationException e) {
                log.error("Constraint violation: {}", e.getMessage());
                dropSchema(schemaName);
                String userMessage = exceptionHelper.getUserFriendlyConstraintMessage(e);
                throw new CommonExceptionHandler.ConflictException(userMessage);

            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation: {}", e.getMessage());
                dropSchema(schemaName);
                String userMessage = exceptionHelper.extractConstraintMessage(e);
                throw new CommonExceptionHandler.ConflictException(userMessage);
            }

            log.info("Organization + Tenant setup completed for OrgId: {}", orgId);
            return orgModel;

        } catch (CommonExceptionHandler.ConflictException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Organization creation failed. Dropping schema {}. Cause: {}", schemaName, e.getMessage(), e);
            dropSchema(schemaName);
            throw new CommonExceptionHandler.InternalServerException(
                    "Failed to create organization or SuperAdmin user"
            );
        } finally {
            TenantContext.clear();
        }
    }

    private OrganizationEntity saveOrganizationPublic(OrganizationEntity entity) {
        try {
            return organizationAdapter.create(entity);
        } catch (Exception e) {
            log.error("Failed to save organization in public schema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save organization in public schema", e);
        }
    }

    private void createSchema(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            log.info("Schema {} created successfully", schemaName);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create schema: " + schemaName, e);
        }
    }

    private void initSchemaTables(String schemaName, String orgType) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            Liquibase liquibase = new Liquibase(
                    "db/changelog/schema_template.sql",
                    new ClassLoaderResourceAccessor(),
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn))
            );
            liquibase.getDatabase().setDefaultSchemaName(schemaName);
            liquibase.setChangeLogParameter("schemaName", schemaName);
            liquibase.setChangeLogParameter("orgType", orgType);
            liquibase.update(new Contexts(), new LabelExpression());
            conn.commit();
            log.info("Tables created in schema {}", schemaName);
        } catch (Exception e) {
            log.error("Liquibase failed for schema {}. Dropping schema. Cause: {}", schemaName, e.getMessage(), e);
            dropSchema(schemaName);
            throw new RuntimeException("Failed to initialize schema tables for: " + schemaName, e);
        }
    }

    private void dropSchema(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
            log.warn("Schema {} dropped successfully", schemaName);
        } catch (SQLException e) {
            log.error("Failed to drop schema {}: {}", schemaName, e.getMessage(), e);
        }
    }

    /**
     * Validate the Organization name
     *
     * @param organization
     */
    @Override
    public Organization validate(Organization organization) {
        OrganizationEntity organizationEntity = organizationEntityMapper.toEntity(organization);
        OrganizationEntity response = organizationAdapter.findByOrgName(organizationEntity.getOrgName());
        if (response != null) {
            if (organization.getOrgName().equalsIgnoreCase(response.getOrgName())) {
                throw new RuntimeException("Name already Exists under other organization");
            }
        }
        return organizationEntityMapper.toModel(response);
    }

    /**
     * @return List of organization type
     */
    @Override
    public List<OrganizationTypeEntity> getOrgType() {
        return organizationAdapter.getAllOrgType();
    }

    /**
     * @param orgId Validate Location and WorkSchedule of the organization
     * @return boolean based on location & WS
     */
    @Override
    public OrgSetupValidationResponse getValidation(String orgId) {
        List<LocationEntity> locationEntities = locationAdapter.findLocation(orgId);
        List<WorkScheduleEntity> workScheduleEntities = workScheduleAdapter.findAllScheduleById(orgId);
        boolean hasLocations = locationEntities != null && !locationEntities.isEmpty();
        boolean hasSchedules = workScheduleEntities != null && !workScheduleEntities.isEmpty();
        return new OrgSetupValidationResponse(hasLocations, hasSchedules);
    }

    @Override
    public OrganizationType getUserOrgType(String orgId) {
        OrganizationEntity org = organizationAdapter.findByOrgId(orgId)
                .orElseThrow(() -> new RuntimeException("No organization found"));

        if (org.getOrgType() == null) {
            throw new RuntimeException("Organization type not found");
        }

        OrganizationTypeEntity orgTypeEntity = organizationAdapter.findOrgType(org.getOrgType());
        OrganizationType dto = organizationEntityMapper.toModel(orgTypeEntity);
        if ("Academic".equalsIgnoreCase(orgTypeEntity.getOrgTypeName())) {
            dto.setShowFilter(true);
        } else {
            dto.setShowFilter(false);
        }
        return dto;
    }

    @Override
    public List<Role> getAllRole(String orgId, String role) {

        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        int hierarchyLevel = UserRole.getLevel(role);
        // Step 1: Get orgType from orgId
        String orgTypeId = organizationRepository.findOrgTypeByOrganizationId(orgId);
        log.info("orgtype:{}", orgTypeId);
        OrganizationTypeEntity orgType = organizationTypeRepository.findById(orgTypeId).orElseThrow(() ->
                new RuntimeException("Org Type not found for id: " + orgTypeId));
        log.info("Org type:{}", orgType);
        // Step 2: Get roles above current hierarchy level
        List<RoleEntity> roleEntities = organizationAdapter.getAllRole(hierarchyLevel);

        // Step 3: Filter STUDENT if org type is not ACADEMIC
        if (!"ACADEMIC".equalsIgnoreCase(orgType.getOrgTypeName())) {
            roleEntities = roleEntities.stream()
                    .filter(r -> !RoleName.STUDENT.getRoleName().equalsIgnoreCase(r.getName()))
                    .toList();
        }

        // Step 4: Map to middleware
        return roleEntities.stream()
                .map(organizationEntityMapper::toMiddleware)
                .toList();
    }

    @Override
    public Privilege addPrivileges(Privilege privilegeModel, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        PrivilegeEntity privilegeEntity = organizationEntityMapper.toEntity(privilegeModel);
        PrivilegeEntity privilege = organizationAdapter.addPrivilege(privilegeEntity);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getRoleprivilege(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("Privilege CacheReloadEvent published after Privilege Creation");
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache add privilege reload.");
        }
        return organizationEntityMapper.toModel(privilege);
    }

    @Override
    public RolePrivilege addRolwisePrivileges(RolePrivilege rolePrivilege, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        RoleEntity role = organizationAdapter.findRoleById(rolePrivilege.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        PrivilegeEntity privilegeEntity = organizationAdapter.findPrivilegeById(rolePrivilege.getPrivilegeId())
                .orElseThrow(() -> new RuntimeException("Privilege not found"));

        if (rolePrivilege.isType()) {
            boolean exists = role.getPrivilegeMappings().stream()
                    .anyMatch(mapping -> mapping.getPrivilege().equals(privilegeEntity));
            if (!exists) {
                RolePrivilegeMapEntity mapping = new RolePrivilegeMapEntity();
                mapping.setRole(role);
                mapping.setPrivilege(privilegeEntity);
                mapping.setEnabled(true);
                mapping.setCreatedAt(LocalDateTime.now());
                role.getPrivilegeMappings().add(mapping);
            }
        } else {
            role.getPrivilegeMappings().removeIf(mapping -> mapping.getPrivilege().equals(privilegeEntity));
        }

        organizationAdapter.saveRole(role);

        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getRoleprivilege(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("RolewisePrivilege CacheReloadEvent published after Role wise privilege creation");
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache add rolewise privilege reload.");
        }
        return rolePrivilege;
    }

    @Override
    public OrganizationSummaryDto getOrgSummary(String orgId) {
        try {
            OrganizationSummaryDto dto = organizationAdapter.getOrgSummary(orgId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Organization not found for id: " + orgId));


            if (dto.getType() != null) {
                String typeName = organizationAdapter.getOrgTypeNameById(dto.getType());
                dto.setType(typeName);
            }


            int total = userAdapter.countTotalMembers(orgId);
            int active = userAdapter.countActiveMembers(orgId);
            int inactive = userAdapter.countInactiveMembers(orgId);
            int subscribed = (int) subscriptionService.getSubscribedUserCount(orgId);

            OrganizationSummaryDto.Counts counts = new OrganizationSummaryDto.Counts();
            counts.setTotalMembers(total);
            counts.setActiveMembers(active);
            counts.setInactiveMembers(inactive);
            counts.setAddedUsers(total);
            counts.setSubscriptionUserCount(subscribed);

            counts.setAvailableSubscriptionSlots(Math.max(0, subscribed - total));

            dto.setCounts(counts);
            return dto;

        } catch (Exception ex) {
            log.info("Error in getOrgSummary for orgId:{}", orgId);
            ex.printStackTrace();
            throw new RuntimeException("Failed to fetch organization summary: " + ex.getMessage(), ex);
        }
    }

    //    @Override
//    public List<OrganizationDetailsDto> getAllOrganizationDetails() {
//        List<OrganizationEntity> organizations = organizationAdapter.findAll();
//        List<OrganizationDetailsDto> dtoList = new ArrayList<>();
//
//        for (OrganizationEntity orgEntity : organizations) {
//            OrganizationDetailsModel model = mapper.toModel(orgEntity);
//            model.setActiveUsers(userAdapter.countActiveMembers(orgEntity.getOrganizationId()));
//            model.setInactiveUsers(userAdapter.countInactiveMembers(orgEntity.getOrganizationId()));
//            dtoList.add(mapper.toDto(model));
//
//
//            return dtoList;
    @Override
    public List<OrganizationDetailsDto> getAllOrganizationDetails() {
        List<OrganizationEntity> orgEntities = organizationAdapter.findAll();
        List<OrganizationDetailsDto> dtoList = new ArrayList<>();

        for (OrganizationEntity orgEntity : orgEntities) {
            OrganizationDetailsModel model = mapper.toModel(orgEntity);

            String orgTypeName = organizationAdapter.getOrgTypeNameById(model.getOrgType());
            if (orgTypeName != null && !orgTypeName.isEmpty()) {
                model.setOrgType(orgTypeName);
            } else {
                model.setOrgType(null);
            }
            model.setActiveUsers(userAdapter.countActiveMembers(orgEntity.getOrganizationId()));
            model.setInactiveUsers(userAdapter.countInactiveMembers(orgEntity.getOrganizationId()));

            SubscriptionDto activePlan = subscriptionAdapter.getActivePlan(orgEntity.getOrganizationId());
            SubscriptionDto subscriptionDto = new SubscriptionDto();

            if (activePlan != null) {
                subscriptionDto.setPlanName(activePlan.getPlanName());
                subscriptionDto.setStart(activePlan.getStart());
                subscriptionDto.setActiveUntil(activePlan.getActiveUntil());
                subscriptionDto.setBillingCycle(activePlan.getBillingCycle());
                subscriptionDto.setStatus(activePlan.getStatus());
                subscriptionDto.setSubscribedUsers(
                        activePlan.getSubscribedUsers() != null ? activePlan.getSubscribedUsers() : 0
                );
            } else {
                subscriptionDto.setPlanName("No active plan");
                subscriptionDto.setStatus("Inactive");
            }

            OrganizationDetailsDto dto = mapper.toDto(model);
            dto.setSubscriptionSummary(subscriptionDto);

            dtoList.add(dto);
        }

        return dtoList;
    }

//    @Override
//    public OrganizationUserCountResponse getUserCountsForOrganization(String orgId, LocalDate fromDate, LocalDate toDate) {
//
//        OrganizationEntity org = organizationRepository.findById(orgId)
//                .orElseThrow(() -> new RuntimeException("Organization not found: " + orgId));
//
//        LocalDateTime start = fromDate.atStartOfDay();
//        LocalDateTime end = toDate.atTime(23, 59, 59);
//
//        // previous month period
//        LocalDate prevFromDate = fromDate.minusMonths(1);
//        LocalDate prevToDate = toDate.minusMonths(1);
//        LocalDateTime prevStart = prevFromDate.atStartOfDay();
//        LocalDateTime prevEnd = prevToDate.atTime(23, 59, 59);
//
//        long currentCount = userAdapter.getUserCount(orgId, start, end);
//        long previousCount = userAdapter.getUserCount(orgId, prevStart, prevEnd);
//
//        return new OrganizationUserCountResponse(orgId, org.getOrgName(), currentCount, previousCount);
//    }


    @Override
    public OrganizationCountResponseDto getOrganizationCount(DateRangeRequestDto request) {

        // Current period
        LocalDateTime from = request.getFromDate().atStartOfDay();
        LocalDateTime to = request.getToDate().atTime(23, 59, 59);
        long currentPeriodCount = organizationRepository.countOrganizationsBetweenDates(from, to);

        // Calculate previous period of same length
        long days = java.time.temporal.ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate());
        LocalDateTime prevFrom = request.getFromDate().minusDays(days + 1).atStartOfDay();
        LocalDateTime prevTo = request.getToDate().minusDays(days + 1).atTime(23, 59, 59);
        long previousPeriodCount = organizationRepository.countOrganizationsBetweenDates(prevFrom, prevTo);

        return new OrganizationCountResponseDto(currentPeriodCount, previousPeriodCount);
    }

    @Override
    public List<OrganizationTypeCountDto> calculateOrganizationTypeCounts(LocalDateTime from, LocalDateTime to) {
        // Fetch raw counts from adapter
        List<Object[]> rawCounts;
        if (from != null && to != null) {
            rawCounts = organizationAdapter.countOrganizationsByTypeBetweenDates(from, to);
        } else {
            rawCounts = organizationAdapter.countOrganizationsByType();
        }

        // Calculate total organizations for percentage
        long totalOrgs = rawCounts.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        return mapToDto(rawCounts, totalOrgs);
    }

    private List<OrganizationTypeCountDto> mapToDto(List<Object[]> rawCounts, long totalOrgs) {
        List<OrganizationTypeCountDto> result = new ArrayList<>();
        for (Object[] row : rawCounts) {
            String orgTypeId = (String) row[0];
            Long count = (Long) row[1];

            // Get org type name dynamically from org_type table
            String orgTypeName = organizationAdapter.findOrgTypeNameById(orgTypeId).orElse(orgTypeId);

            // Calculate percentage
            BigDecimal percentage = totalOrgs > 0
                    ? BigDecimal.valueOf(count * 100.0 / totalOrgs).setScale(2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;

            result.add(new OrganizationTypeCountDto(orgTypeId, orgTypeName, count, percentage));
        }
        return result;
    }


}




