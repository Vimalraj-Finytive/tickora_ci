package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.config.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.OrgSetupValidationResponse;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.enums.CountryEnum;
import com.uniq.tms.tms_microservice.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.Organization;
import com.uniq.tms.tms_microservice.model.OrganizationType;
import com.uniq.tms.tms_microservice.service.IdGenerationService;
import com.uniq.tms.tms_microservice.service.OrganizationService;
import com.uniq.tms.tms_microservice.service.UserService;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LogManager.getLogger(OrganizationServiceImpl.class);
    private final UserEntityMapper userEntityMapper;
    private final UserAdapter userAdapter;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final IdGenerationService idGenerationService;
    private final DataSource dataSource;

    @Autowired
    private UserService userService;

    public OrganizationServiceImpl(UserEntityMapper userEntityMapper, UserAdapter userAdapter, WorkScheduleAdapter workScheduleAdapter, IdGenerationService idGenerationService, DataSource dataSource) {
        this.userEntityMapper = userEntityMapper;
        this.userAdapter = userAdapter;
        this.workScheduleAdapter = workScheduleAdapter;
        this.idGenerationService = idGenerationService;
        this.dataSource = dataSource;
    }

    /**
     *
     * @param organization
     * @Create organization and Org Superadmin
     * @return SuccessT
     */
    @Override
    @Transactional
    public Organization create(Organization organization) {
        String schemaName = organization.getOrgName().toLowerCase().replaceAll("\\s+", "_");
        log.info("Testing");
        OrganizationEntity entity = userEntityMapper.toEntity(organization);

        // Generate orgId
        String prefix = idGenerationService.generateOrgPrefix(organization.getOrgName()).toUpperCase();
        Long count = userAdapter.countOrganizations();
        String formattedNumber = String.format("%04d", count);
        String orgId = prefix + formattedNumber;

        entity.setOrganizationId(orgId);
        entity.setTimeZone(CountryEnum.getTimeZoneByCountry(organization.getCountry()));
        log.info("Getting Org Range from Enum:{}", organization.getOrgSize());
        log.info("orgRange:{}", entity.getOrgSize());
        entity.setSchemaName(schemaName);

        if (userAdapter.existsByOrganizationId(orgId)) {
            throw new RuntimeException("Organization ID already exists: " + orgId);
        }

        try {
            OrganizationEntity savedOrg = saveOrganizationPublic(entity);
            Organization orgModel = userEntityMapper.toModel(savedOrg);

            createSchema(schemaName);
            initSchemaTables(schemaName, organization.getOrgType());

            TenantContext.setCurrentTenant(schemaName);

            ApiResponse response = userService.createSuperAdminUser(organization, orgId, schemaName);

            TenantContext.setCurrentTenant("public");

            log.info("setting user map:{}", orgId);
            UserSchemaMappingEntity mapping = userEntityMapper.toSchema(
                    organization.getEmail(),
                    organization.getMobile(),
                    orgId,
                    schemaName
            );
            userAdapter.create(mapping);

            log.info("Organization + Tenant setup completed for OrgId: {}", orgId);
            return orgModel;

        } catch (Exception e) {
            log.error("Organization creation failed. Dropping schema {}. Cause: {}", schemaName, e.getMessage(), e);
            dropSchema(schemaName);
            throw new CommonExceptionHandler.InternalServerException(
                    "Failed to create organization or SuperAdmin user: " + e.getMessage()
            );
        } finally {
            TenantContext.clear();
        }
    }

    private OrganizationEntity saveOrganizationPublic(OrganizationEntity entity) {
        try {
            return userAdapter.create(entity);
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
