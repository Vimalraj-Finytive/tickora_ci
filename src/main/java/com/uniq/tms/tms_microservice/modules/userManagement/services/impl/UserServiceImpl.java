package com.uniq.tms.tms_microservice.modules.userManagement.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.CalendarAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyBulkAssignModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffPolicyService;
import com.uniq.tms.tms_microservice.modules.locationManagement.adapter.LocationAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.mapper.LocationDtoMapper;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.LocationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.OrganizationEntityMapper;
import com.uniq.tms.tms_microservice.shared.event.*;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserProjection;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.RoleEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.OrganizationStatusEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationCacheService;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.*;
import com.uniq.tms.tms_microservice.modules.userManagement.model.*;
import com.uniq.tms.tms_microservice.shared.helper.*;
import com.uniq.tms.tms_microservice.shared.util.*;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.authenticationManagement.model.EmailData;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Organization;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.MemberType;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.RoleName;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.RoleRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.GroupRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserCacheService;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheKeyConfig;
import com.uniq.tms.tms_microservice.shared.security.cache.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.shared.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.shared.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.SecondaryDetailsMapper;
import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Validator;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.opencsv.CSVReader;
import static com.uniq.tms.tms_microservice.shared.util.TextUtil.isBlank;

@Service
public class UserServiceImpl implements UserService {

    private final UserAdapter userAdapter;
    private final TimesheetAdapter timesheetAdapter;
    private final UserEntityMapper userEntityMapper;
    private final CalendarAdapter calendarAdapter;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;
    private final EmailHelper emailHelper;
    private final UserDtoMapper userDtoMapper;
    private final SecondaryDetailsMapper secondaryDetailsMapper;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserCacheService userCacheService;
    private final ApplicationEventPublisher publisher;
    private final CacheKeyUtil cacheKeyUtil;
    private final GroupRepository groupRepository;
    private final CacheKeyConfig cacheKeyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;
    private final IdGenerationService idGenerationService;
    private final RolePrivilegeHelper rolePrivilegeHelper;
    private final ExceptionHelper exceptionHelper;
    private final OrganizationCacheService organizationCacheService;
    private final LocationDtoMapper locationDtoMapper;
    private final OrganizationAdapter organizationAdapter;
    private final LocationAdapter locationAdapter;
    private final OrganizationEntityMapper organizationEntityMapper;
    private final AuthHelper authHelper;
    private final TimeOffPolicyAdapter timeOffPolicyAdapter;
    private final TimeOffPolicyService timeOffPolicyService;
    private final UserMergeUtil userMergeUtil;
    public UserServiceImpl(Validator validator, UserAdapter userAdapter, TimesheetAdapter timesheetAdapter,
                           UserEntityMapper userEntityMapper, CalendarAdapter calendarAdapter, OrganizationRepository organizationRepository,
                           RoleRepository roleRepository, LocationRepository locationRepository, EmailHelper emailHelper,
                           UserDtoMapper userDtoMapper, SecondaryDetailsMapper secondaryDetailsMapper,
                           @Nullable RedisTemplate<String, Object> redisTemplate,
                           ApplicationEventPublisher publisher, WorkScheduleAdapter workScheduleAdapter, UserCacheService userCacheService,
                           CacheKeyUtil cacheKeyUtil, GroupRepository groupRepository, CacheKeyConfig cacheKeyConfig,
                           CacheReloadHandlerRegistry cacheReloadHandlerRegistry, IdGenerationService idGenerationService, RolePrivilegeHelper rolePrivilegeHelper,
                           ExceptionHelper exceptionHelper, OrganizationCacheService organizationCacheService, LocationDtoMapper locationDtoMapper,
                           OrganizationAdapter organizationAdapter, LocationAdapter locationAdapter, OrganizationEntityMapper organizationEntityMapper,
                           AuthHelper authHelper, TimeOffPolicyAdapter timeOffPolicyAdapter, TimeOffPolicyService timeOffPolicyService,
                           UserMergeUtil userMergeUtil) {
        this.userAdapter = userAdapter;
        this.timesheetAdapter = timesheetAdapter;
        this.userEntityMapper = userEntityMapper;
        this.calendarAdapter = calendarAdapter;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.emailHelper = emailHelper;
        this.userDtoMapper = userDtoMapper;
        this.secondaryDetailsMapper = secondaryDetailsMapper;
        this.workScheduleAdapter = workScheduleAdapter;
        this.redisTemplate = redisTemplate;
        this.userCacheService = userCacheService;
        this.publisher = publisher;
        this.cacheKeyUtil = cacheKeyUtil;
        this.groupRepository = groupRepository;
        this.cacheKeyConfig = cacheKeyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
        this.idGenerationService = idGenerationService;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
        this.exceptionHelper = exceptionHelper;
        this.organizationCacheService = organizationCacheService;
        this.locationDtoMapper = locationDtoMapper;
        this.organizationAdapter = organizationAdapter;
        this.locationAdapter = locationAdapter;
        this.organizationEntityMapper = organizationEntityMapper;
        this.authHelper = authHelper;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
        this.timeOffPolicyService = timeOffPolicyService;
        this.userMergeUtil = userMergeUtil;
    }

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${csv.upload.dir}")
    private String uploadDir;

    @Value("${cache.redis.enabled}")
    private boolean isRedisEnabled;

    @Value("${basic.plan.max.users}")
    private int subscribedUsers;

    @Override
    public List<Group> getAllGroup(String orgId) {
        return userAdapter.getAllGroup(orgId).stream().map(userEntityMapper::toMiddleware).toList();
    }

    @Override
    public ApiResponse bulkCreateUsers(MultipartFile file, String orgId, String userId) {
        log.info("Checking work flow for create user");
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null || fileName == null) {
            throw new RuntimeException("Invalid file input.");
        }
        if (fileName.endsWith(".csv") || contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel")) {
            try {
                String filePath = saveCsvToLocal(file);
                return processCsvFileFromPath(filePath, orgId, userId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Unsupported file type. Only CSV files are supported.");
        }
    }

    public ApiResponse processCsvFileFromPath(String filePath, String orgId, String userId) {
        File file = new File(filePath);
        if (!file.exists()) throw new RuntimeException("CSV file not found: " + filePath);

        try (InputStream inputStream = new FileInputStream(file)) {
            return processCsvFile(inputStream, file.getName(), orgId, userId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read local CSV file: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ApiResponse processCsvFile(InputStream inputStream, String originalFileName, String orgId, String userId) {

        long startTime = System.currentTimeMillis();
        log.info("Bulk upload started for orgId={}, file={}", orgId, originalFileName);

        UserEntity userFromToken = userAdapter.findUserByOrgIdAndUserId(orgId, userId);
        String schema = TenantUtil.getCurrentTenant();

        List<Map<String, Object>> skippedRows = new ArrayList<>();
        List<UserEntity> userEntities = new ArrayList<>();
        List<SecondaryDetailsEntity> secondaryDetailsEntities = new ArrayList<>();
        List<EmailData> emailRequests = new ArrayList<>();
        List<String> successList = new ArrayList<>();

        try {

            // ==== Fetch existing DB and schema data ====
            Set<String> existingEmails = userAdapter.getAllEmails(orgId);
            Set<String> existingMobiles = userAdapter.getAllMobileNumbers(orgId);
            Set<String> existingSecEmails = userAdapter.getAllSecondaryEmail(orgId);
            Set<String> existingSecMobiles = userAdapter.getAllSecondaryMobile(orgId);
            Set<String> schemaEmails = userAdapter.getAllMappedEmails(orgId);
            Set<String> schemaMobiles = userAdapter.getAllMappedMobiles(orgId);
            Map<String, Long> roleMap = organizationAdapter.getRoleNameIdMap();
            Map<String, Long> locationMap = locationAdapter.getLocationNameToIdMap(orgId);
            Map<String, Long> groupMap = userAdapter.getGroupNameIdMap(orgId);

            Map<String, String> workScheduleMap = workScheduleAdapter.getAllSchedules(orgId)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toLowerCase(),
                            Map.Entry::getValue
                    ));

            List<String> expectedHeaders = List.of(
                    "username", "email", "mobilenumber", "rolename", "locationname", "dateofjoining",
                    "secondaryusername", "secondarymobile", "secondaryemail", "relation", "groupname", "workschedule"
            );

            List<BulkUserHelper> allRecords = new ArrayList<>();
            Set<String> fileEmails = new HashSet<>();
            Set<String> fileMobiles = new HashSet<>();
            int rowNumber = 1;

            try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
                String[] headerRow = reader.readNext();
                if (headerRow == null)
                    throw new RuntimeException("Missing header row");
                validateFixedHeaders(headerRow, expectedHeaders);

                String[] row;
                while ((row = reader.readNext()) != null) {
                    rowNumber++;
                    BulkUserHelper record = new BulkUserHelper(rowNumber, row);

                    String username = row[0].trim();
                    String email = row[1].trim().toLowerCase();
                    String mobile = row[2].trim();
                    String roleName = row[3].trim().toLowerCase();
                    String locationName = row[4].trim();
                    String doj = row[5].trim();
                    String secName = row[6].trim();
                    String secMobile = row[7].trim();
                    String secEmail = row[8].trim().toLowerCase();
                    String relation = row[9].trim();
                    String groupName = row[10].trim();
                    String workSchedule = row[11].trim().toLowerCase();

                    // ====== MANDATORY FIELD CHECK ======
                    if (Stream.of(username, email, mobile, roleName, locationName, doj, workSchedule).anyMatch(StringUtils::isBlank)) {
                        record.invalidate("Mandatory fields missing");
                        allRecords.add(record);
                        continue;
                    }

                    // ====== VALIDATE USERNAME ======
                    if (!username.matches("^[A-Za-z ]+$")) {
                        record.invalidate("Username must contain only letters and spaces");
                        allRecords.add(record);
                        continue;
                    }

                    // ====== DUPLICATE WITHIN FILE ======
                    if (!fileEmails.add(email) || !fileMobiles.add(mobile)) {
                        record.invalidate("Duplicate email or mobile within file");
                        allRecords.add(record);
                        continue;
                    }

                    // ====== EXISTING EMAIL/MOBILE IN DB OR SCHEMA ======
                    if (existingEmails.contains(email) || existingMobiles.contains(mobile)
                            || schemaEmails.contains(email) || schemaMobiles.contains(mobile)) {
                        record.invalidate("Email or mobile already exists");
                        allRecords.add(record);
                        continue;
                    }

                    // ====== VALIDATE ROLE ======
                    Long roleId = roleMap.get(roleName);
                    if (roleId == null) {
                        record.invalidate("Invalid role: " + roleName);
                        allRecords.add(record);
                        continue;
                    }

                    // ====== VALIDATE WORK SCHEDULE ======
                    String scheduleId = workScheduleMap.get(workSchedule);
                    if (scheduleId == null) {
                        record.invalidate("Invalid work schedule: " + workSchedule);
                        allRecords.add(record);
                        continue;
                    }

                    // ====== VALIDATE LOCATIONS ======
                    String[] locationList = locationName.split(",");
                    List<Long> validLocIds = new ArrayList<>();
                    for (String loc : locationList) {
                        Long locId = locationMap.get(loc.trim().toLowerCase());
                        if (locId != null) validLocIds.add(locId);
                    }
                    if (validLocIds.isEmpty()) {
                        record.invalidate("Invalid location(s): " + locationName);
                        allRecords.add(record);
                        continue;
                    }

                    // ====== VALIDATE SECONDARY DETAILS ======
                    String privilegeKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.HAVE_SECONDARY_DETAILS);
                    boolean hasSecondaryPrivilege = rolePrivilegeHelper.roleHasPrivilege(roleName, privilegeKey);

                    boolean anySecondaryFilled = Stream.of(secName, secMobile, secEmail, relation).anyMatch(StringUtils::isNotBlank);
                    boolean allSecondaryFilled = Stream.of(secName, secMobile, secEmail, relation).allMatch(StringUtils::isNotBlank);

                    if (hasSecondaryPrivilege) {
                        if (anySecondaryFilled && !allSecondaryFilled) {
                            record.invalidate("All secondary fields (name, mobile, email, relation) must be filled if one is provided");
                            allRecords.add(record);
                            continue;
                        }

                        if (!anySecondaryFilled) {
                            record.invalidate("Secondary details are mandatory for this role");
                            allRecords.add(record);
                            continue;
                        }

                        if (existingSecEmails.contains(secEmail) || existingSecMobiles.contains(secMobile)
                                || existingEmails.contains(secEmail) || existingMobiles.contains(secMobile)) {
                            record.invalidate("Secondary email or mobile already exists");
                            allRecords.add(record);
                            continue;
                        }

                        if (mobile.equals(secMobile)) {
                            record.invalidate("Primary and secondary mobile numbers cannot be the same");
                            allRecords.add(record);
                            continue;
                        }

                        record.markHasSecondary(secName, secMobile, secEmail, relation);
                    } else {
                        if (anySecondaryFilled) {
                            record.invalidate("Secondary details are not required for this role. Please leave them blank.");
                            allRecords.add(record);
                            continue;
                        }
                    }

                    // ====== MARK VALID RECORD ======
                    record.markValid(email, roleId, validLocIds, scheduleId, groupName,
                            secName, secMobile, secEmail, relation);
                    allRecords.add(record);
                }
            }

            // ====== SUBSCRIPTION LIMIT CHECK ======
            long currentCount = userAdapter.getCurrentUserCount(orgId);
            long maxUsers = userAdapter.getSubscribedUserLimit(orgId);
            long remainingSlots = maxUsers - currentCount;

            List<BulkUserHelper> validRecords = allRecords.stream().filter(BulkUserHelper::isValid).toList();
            List<BulkUserHelper> invalidRecords = new ArrayList<>(allRecords.stream().filter(r -> !r.isValid()).toList());

            if (remainingSlots <= 0) {
                invalidRecords.addAll(validRecords.stream()
                        .peek(r -> r.invalidate("Subscription limit reached"))
                        .toList());
                validRecords = List.of();
            } else if (validRecords.size() > remainingSlots) {
                List<BulkUserHelper> allowed = validRecords.subList(0, (int) remainingSlots);
                List<BulkUserHelper> excess = validRecords.subList((int) remainingSlots, validRecords.size());
                excess.forEach(r -> r.invalidate("Subscription limit reached"));
                invalidRecords.addAll(excess);
                validRecords = allowed;
            }

            // ====== SAVE VALID USERS ======
            for (BulkUserHelper record : validRecords) {
                UserDto userDto = record.toUserDto(orgId, idGenerationService);
                String defaultPass = PasswordUtil.generateDefaultPassword();
                UserEntity userEntity = createUserEntity(userDto, orgId, defaultPass);
                userEntities.add(userEntity);
                emailRequests.add(new EmailData(userDto.getEmail(), userDto.getUserName(), defaultPass,
                        userDto.isRegisterUser(), userDto.getRoleId()));
                successList.add(userDto.getUserName());
            }

            userAdapter.saveAllUsers(userEntities);
            Map<String, UserEntity> savedUserByEmail = userEntities.stream()
                    .collect(Collectors.toMap(
                            u -> u.getEmail().trim().toLowerCase(),
                            Function.identity(),
                            (u1, u2) -> u1
                    ));

            // ====== GROUP MAPPING ======
            List<UserGroupEntity> groupEntities = validRecords.stream()
                    .flatMap(record -> {
                        UserEntity savedUser = savedUserByEmail.get(record.getEmail().trim().toLowerCase());
                        if (savedUser == null || StringUtils.isBlank(record.getGroupName())) return Stream.empty();

                        return Arrays.stream(record.getGroupName().split(","))
                                .map(String::trim)
                                .filter(StringUtils::isNotBlank)
                                .map(String::toLowerCase)
                                .map(groupMap::get)
                                .filter(Objects::nonNull)
                                .map(gId -> {
                                    UserGroupEntity ug = new UserGroupEntity();
                                    ug.setUser(savedUser);
                                    ug.setGroup(new GroupEntity(gId));
                                    return ug;
                                });
                    }).toList();

            userAdapter.saveAllUserGroups(groupEntities);

            // ====== LOCATION MAPPING ======
            List<UserLocationEntity> userLocationEntities = validRecords.stream()
                    .flatMap(record -> {
                        UserEntity savedUser = savedUserByEmail.get(record.getEmail().trim().toLowerCase());
                        if (savedUser == null) return Stream.empty();
                        return record.getLocationIds().stream().map(locId -> {
                            UserLocationEntity ul = new UserLocationEntity();
                            ul.setUser(savedUser);
                            ul.setLocation(new LocationEntity(locId));
                            return ul;
                        });
                    }).toList();

            locationAdapter.saveUserLocation(userLocationEntities);

            // ====== SECONDARY DETAILS ======
            for (BulkUserHelper record : validRecords) {
                if (record.hasSecondary()) {
                    UserEntity savedUser = savedUserByEmail.get(record.getEmail());
                    if (savedUser != null) {
                        SecondaryDetailsEntity secEntity = createSecondaryDetails(savedUser,
                                record.getSecondaryMobile(), record.getSecondaryEmail(),
                                record.getSecondaryName(), record.getRelation());
                        secondaryDetailsEntities.add(secEntity);
                    }
                }
            }

            userAdapter.saveAllSecondaryDetails(secondaryDetailsEntities);

            List<String> userIds = userEntities.stream()
                    .map(UserEntity::getUserId)
                    .toList();

            TimeOffPolicyEntity defaultPolicyEntity = timeOffPolicyAdapter.findDefaultPolicy();
            if (defaultPolicyEntity == null) {
                log.warn("Default time-off policy not found for orgId={}", orgId);
            } else {
                TimeOffPolicyBulkAssignModel assignModel = new TimeOffPolicyBulkAssignModel();
                assignModel.setUserIds(userIds);
                assignModel.setPolicyId(defaultPolicyEntity.getPolicyId());
                assignModel.setUserValidFrom(LocalDate.now());

                try {
                    timeOffPolicyService.assignPolicies(assignModel);
                } catch (Exception ex) {
                    log.error("Default policy assignment failed for {} users. Error: {}", userIds.size(), ex.getMessage(), ex);

                }
            }

            int uploadedCount = userEntities.size();
            int skippedCount = invalidRecords.size();
            sendEmailsAsync(emailRequests);
            emailHelper.sendSuccessEmail(userFromToken.getEmail(), userFromToken.getUserName(),
                    uploadedCount, skippedCount);
            if (isRedisEnabled) {
                try {
                    publisher.publishEvent(new UserEvent(orgId, authHelper.getSchema()));
                    log.info("UserCacheReloadEvent published after bulk user upload for orgId={}", orgId);
                } catch (Exception e) {
                    log.error("Failed to publish UserCacheReloadEvent for orgId={}", orgId, e);
                }
            } else {
                log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload of bulk User members for orgId={}", orgId);
            }
            log.info("Upload complete. Uploaded={}, Skipped={}, Time={}ms",
                    uploadedCount, skippedCount, (System.currentTimeMillis() - startTime));

            return new ApiResponse(200,
                    uploadedCount + " users created. " + skippedCount + " skipped.",
                    null);

        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV: " + e.getMessage(), e);
        }
    }

    public String saveCsvToLocal(MultipartFile file) throws IOException {
        Path tempDirPath = Paths.get(uploadDir);
        Files.createDirectories(tempDirPath);

        String originalFilename = file.getOriginalFilename();
        String baseName = FilenameUtils.getBaseName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);

        assert originalFilename != null;
        Path destinationPath = tempDirPath.resolve(originalFilename);
        int count = 1;

        while (Files.exists(destinationPath)) {
            String newFileName = baseName + "(" + count + ")." + extension;
            destinationPath = tempDirPath.resolve(newFileName);
            count++;
        }
        file.transferTo(destinationPath.toFile());
        System.out.println("File saved at: " + destinationPath.toAbsolutePath());

        return destinationPath.toAbsolutePath().toString();
    }

    private UserEntity createUserEntity(UserDto userDto, String orgId, String generatedPass) {
        UserEntity entity = userEntityMapper.toEntity(userDtoMapper.toMiddleware(userDto));
        entity.setOrganizationId(orgId);
        entity.setPassword(PasswordUtil.encryptPassword(generatedPass));
        entity.setDefaultPassword(true);
        entity.setActive(true);
        CalendarEntity calendarEntity = calendarAdapter.findDefaultCalendar();
        entity.setCalendar(calendarEntity);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private SecondaryDetailsEntity createSecondaryDetails(UserEntity user, String secMobile, String secEmail, String secUserName, String relation) {
        SecondaryDetailsEntity entity = new SecondaryDetailsEntity();
        entity.setId(idGenerationService.generateNextSecondaryUserId(user.getOrganizationId()));
        entity.setUser(user);
        entity.setMobile(secMobile);
        if (isBlank(secEmail)) {
            entity.setEmail(null);
        } else {
            entity.setEmail(secEmail);
        }
        entity.setUserName(secUserName);
        entity.setRelation(relation);
        return entity;
    }

    private void validateFixedHeaders(String[] actual, List<String> expected) {
        if (actual.length != expected.size())
            throw new RuntimeException("Header count mismatch. Expected: " + expected.size() + " but found: " + actual.length);

        for (int i = 0; i < expected.size(); i++) {
            String expectedKey = normalizeHeader(expected.get(i));
            String actualKey = normalizeHeader(actual[i]);

            if (!expectedKey.equals(actualKey)) {
                throw new RuntimeException("Header mismatch at position " + (i + 1) +
                        ". Expected: '" + expected.get(i) + "' but found: '" + actual[i] + "'");
            }
        }
    }

    private String normalizeHeader(String header) {
        return header.trim().toLowerCase().replaceAll("[ _]", "");
    }

    @Async
    public void sendEmailsAsync(List<EmailData> emailRequests) {
        int successCount = 0;
        int failureCount = 0;
        List<Map<String, String>> failedEmails = new ArrayList<>();

        for (EmailData emailData : emailRequests) {
            try {
                emailHelper.sendAccountCreationEmail(emailData.getEmail(), emailData.getUserName(), emailData.getGeneratedPass(), emailData.isNewUser(), emailData.getRoleId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send email to {}", emailData.getEmail(), e);
                failureCount++;
                failedEmails.add(Map.of(
                        "email", emailData.getEmail(),
                        "userName", emailData.getUserName(),
                        "reason", e.getMessage()
                ));
                log.error("Email failed for user {} : {}", emailData.getEmail(), e.getMessage());
            }
        }
        log.info("Email success: {} ", successCount);
        log.info("Email failure: {} ", failureCount);

        if (!failedEmails.isEmpty()) {
            log.info("Failed emails Details: {}", failedEmails);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UserDto> createUser(UserDto userDto, SecondaryDetailsDto secondaryDetailsDto, String organizationId) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Checking if the user is student: {}", userDto.getRoleId());
        User userMiddleware = userDtoMapper.toMiddleware(userDto);
        RoleEntity role = roleRepository.findById(userMiddleware.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + userMiddleware.getRoleId()));
        log.info("Role from DB for creating user: {}", role.getName());
        String key = organizationCacheService.getPrivilegeKey(PrivilegeConstants.HAVE_SECONDARY_DETAILS);
        boolean hasSecondaryDetailsPrivilege = rolePrivilegeHelper.roleHasPrivilege(role.getName(), key);
        log.info("hasSecondaryDetailsPrivilege: {}", hasSecondaryDetailsPrivilege);
        if (hasSecondaryDetailsPrivilege) {
            validateSecondaryUser(secondaryDetailsDto);
        }
        validatePrimaryUser(userDto);
        log.info("Creating user: {}", userDto.getUserName());
        UserEntity entity = userEntityMapper.toEntity(userMiddleware);
        entity.setOrganizationId(organizationId);
        String customUserId = idGenerationService.generateNextUserId(organizationId);
        entity.setUserId(customUserId);
        if (isBlank(userMiddleware.getRoleId())) {
            throw new CommonExceptionHandler.BadRequestException("roleId must not be null");
        }

        entity.setRole(role);
        String defaultPassword = PasswordUtil.generateDefaultPassword();
        String encryptedPassword = PasswordUtil.encryptPassword(defaultPassword);
        entity.setPassword(encryptedPassword);
        entity.setDefaultPassword(true);
        WorkScheduleEntity scheduleToSet = null;
        if (userMiddleware.getWorkSchedule() == null || userMiddleware.getWorkSchedule().isEmpty()) {
            scheduleToSet = workScheduleAdapter.findDefaultActiveSchedule(organizationId);
            if (scheduleToSet == null) {
                throw new IllegalStateException("No default work schedule found for this organization");
            }
        } else {
            scheduleToSet = workScheduleAdapter.findByScheduleId(userDto.getWorkSchedule(), organizationId);
        }
        entity.setWorkSchedule(scheduleToSet);
        entity.setActive(true);

        CalendarEntity calendarEntity;
        if (userDto.getCalendarId() != null) {
            calendarEntity = calendarAdapter.findByCalendarId(userDto.getCalendarId())
                    .orElseThrow(() ->
                            new NoSuchElementException("Calendar not found with ID: " + userDto.getCalendarId()));
        } else {
            calendarEntity = calendarAdapter.findDefaultCalendar();
        }
        entity.setCalendar(calendarEntity);
        if (userDto.getRequestApproverId() != null) {
            entity.setRequestApproverId(userDto.getRequestApproverId());
        }
        entity.setCreatedAt(LocalDateTime.now());
        log.info("Saving user: {}", userMiddleware.getUserName());
        UserEntity savedUserEntity = userAdapter.saveUser(entity);
        TimeOffPolicyEntity timeOffPolicyEntity = timeOffPolicyAdapter.findDefaultPolicy();
        assignPolicy(timeOffPolicyEntity.getPolicyId(), customUserId, LocalDate.now());
        List<Long> locationIds = Collections.emptyList();
        List<Long> groupIds = Collections.emptyList();
        SecondaryDetailsEntity saveSecondaryUser = null;
        if (hasSecondaryDetailsPrivilege) {
            log.info("Saving secondary details: {}", secondaryDetailsDto.getUserName());
            SecondaryDetails secondaryDetails = secondaryDetailsMapper.toMiddleware(secondaryDetailsDto);
            SecondaryDetailsEntity secondaryDetailsEntity = secondaryDetailsMapper.toEntity(secondaryDetails);
            log.info("Call id generation service: {}", organizationId);
            String customSecondaryUserId = idGenerationService.generateNextSecondaryUserId(organizationId);
            secondaryDetailsEntity.setId(customSecondaryUserId);
            secondaryDetailsEntity.setUser(savedUserEntity);
            secondaryDetailsEntity.setEmail(TextUtil.trim(secondaryDetails.getEmail()));
            saveSecondaryUser = userAdapter.saveSecondaryDetails(secondaryDetailsEntity);
            log.info("Saved secondary details: {}", secondaryDetails);
        }
        if (savedUserEntity != null) {
            log.info("Creating user mapping for all saved users");
            UserSchemaMappingEntity mappings = organizationEntityMapper.toSchema(
                    savedUserEntity.getEmail(),
                    savedUserEntity.getMobileNumber(),
                    organizationId,
                    TenantContext.getCurrentTenant()
            );
            try {
                userAdapter.create(mappings);
            } catch (ConstraintViolationException e) {
                log.error("Constraint violation: {}", e.getMessage());
                String userMessage = exceptionHelper.getUserFriendlyConstraintMessage(e);
                throw new CommonExceptionHandler.ConflictException(userMessage);

            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation: {}", e.getMessage());
                String userMessage = exceptionHelper.extractConstraintMessage(e);
                throw new CommonExceptionHandler.ConflictException(userMessage);
            }
        }

        if (saveSecondaryUser != null) {
            log.info("Creating user mapping for all saved secondary users");
            UserSchemaMappingEntity secondaryMappings = organizationEntityMapper.toSchema(
                    null,
                    saveSecondaryUser.getMobile(),
                    organizationId,
                    TenantContext.getCurrentTenant()
            );
            try {
                userAdapter.create(secondaryMappings);
            } catch (ConstraintViolationException e) {
                log.error("Constraint violation: {}", e.getMessage());
                String userMessage = exceptionHelper.getUserFriendlyConstraintMessage(e);
                throw new CommonExceptionHandler.ConflictException(userMessage);

            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation: {}", e.getMessage());
                String userMessage = exceptionHelper.extractConstraintMessage(e);
                throw new CommonExceptionHandler.ConflictException(userMessage);
            }
        }
        if (!isBlank(userDto.getLocationId())) {
            log.info("Adding user to location: {}", userDto.getLocationId());
            List<UserLocationEntity> userLocationEntities = new ArrayList<>();
            List<LocationEntity> locations = locationAdapter.findAllLocationById(userDto.getLocationId());
            if (locations.size() != userDto.getLocationId().size()) {
                throw new NoSuchElementException("Location not found ");
            }
            for (LocationEntity location : locations) {
                UserLocationEntity userLocation = new UserLocationEntity();
                userLocation.setUser(savedUserEntity);
                userLocation.setLocation(location);
                userLocationEntities.add(userLocation);
            }
            locationAdapter.saveUserLocation(userLocationEntities);
            locationIds = userLocationEntities.stream()
                    .map(userLocation -> userLocation.getLocation().getLocationId())
                    .toList();
        }

        if (!(userDto.getPolicyIds() == null || userDto.getPolicyIds().isEmpty())) {
            log.info("Adding user to policy: {}", userDto.getPolicyIds());
            LocalDate startDate = userDto.getUserValidFrom();
            userDto.getPolicyIds()
                    .forEach(id -> assignPolicy(id, customUserId, startDate));
        }

        if (!isBlank(userDto.getGroupId())) {
            log.info("Adding user to group: {}", userDto.getGroupId());
            List<UserGroupEntity> userGroupEntities = new ArrayList<>();
            List<GroupEntity> groups = userAdapter.findGroupsByIds(new HashSet<>(userDto.getGroupId()));
            if (groups.size() != userDto.getGroupId().size()) {
                throw new NoSuchElementException("Group not found");
            }
            for (GroupEntity group : groups) {
                UserGroupEntity userGroup = new UserGroupEntity();
                userGroup.setUser(savedUserEntity);
                userGroup.setGroup(group);
                userGroupEntities.add(userGroup);
            }
            userAdapter.saveAllUserGroups(userGroupEntities);
            groupIds = userGroupEntities.stream()
                    .map(userGroup -> userGroup.getGroup().getGroupId())
                    .toList();
        }

        User finalUser = userEntityMapper.toMiddleware(savedUserEntity);
        finalUser.setLocationId(locationIds);
        finalUser.setGroupId(groupIds);
        boolean isNewUser = savedUserEntity.isDefaultPassword();
        emailHelper.sendAccountCreationEmail(
                userMiddleware.getEmail(), userMiddleware.getUserName(), defaultPassword, isNewUser, userMiddleware.getRoleId()
        );

        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new UserEvent(organizationId, authHelper.getSchema()));
                log.info("UserCacheReloadEvent published after User creation");
            } catch (Exception e) {
                log.error("Failed to publish UserCacheReloadEvent for orgId={}", organizationId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload of User members for orgId={}", organizationId);
        }
        return new ApiResponse<>(201, "Successfully saved user", userDto);
    }

    private void assignPolicy(String policyId, String customUserId, LocalDate startDate) {
        TimeOffPolicyBulkAssignModel defaultPolicy = new TimeOffPolicyBulkAssignModel();
        defaultPolicy.setUserIds(List.of(customUserId));
        defaultPolicy.setPolicyId(policyId);
        defaultPolicy.setUserValidFrom(startDate);
        timeOffPolicyService.assignPolicies(defaultPolicy);
    }

    public boolean validateSecondaryUser(SecondaryDetailsDto dto) {
        if (isBlank(dto.getMobile()) || isBlank(dto.getRelation()) || isBlank(dto.getUserName())) {
            log.error("Mandatory secondary user fields must not be null");
            throw new CommonExceptionHandler.BadRequestException("Mandatory secondary user fields must not be null");
        }
        Optional<SecondaryDetailsEntity> mobileExists = userAdapter.findByMobileByMobile(dto.getMobile());
        if (mobileExists.isPresent()) {
            boolean isPrimaryActive = mobileExists.get().getUser().isActive();
            if (!isPrimaryActive) {
                throw new CommonExceptionHandler.BadRequestException(
                        "Inactive User."
                );
            }
            throw new CommonExceptionHandler.BadRequestException(
                    "Secondary User Mobile number already exists."
            );
        }
        Optional<SecondaryDetailsEntity> emailExists = userAdapter.findByEmailByEmail(dto.getEmail());
        if (emailExists.isPresent()) {
            boolean isPrimaryActive = emailExists.get().getUser().isActive();
            if (!isPrimaryActive) {
                throw new CommonExceptionHandler.BadRequestException(
                        "Inactive User."
                );
            }
            throw new CommonExceptionHandler.BadRequestException(
                    "Secondary User Email already exists."
            );
        }
        return true;
    }

    private boolean validatePrimaryUser(UserDto userDto) {
        if (isBlank(String.valueOf(userDto)) || isBlank(userDto.getUserName()) || isBlank(userDto.getMobileNumber()) || isBlank(userDto.getEmail())) {
            log.error("Mandatory primary user fields must not be null");
            throw new CommonExceptionHandler.BadRequestException("Mandatory Primary user fields must not be null");
        }
        Optional<UserEntity> mobilExists = userAdapter.findByMobileNumber(userDto.getMobileNumber());
        if (mobilExists.isPresent()) {
            if (!mobilExists.get().isActive()) {
                throw new CommonExceptionHandler.BadRequestException(
                        "Inactive Users."
                );
            }
            throw new CommonExceptionHandler.BadRequestException(
                    "User with this mobile number already exists."
            );
        }
        Optional<UserEntity> emailExists = userAdapter.findByEmail(userDto.getEmail());
        if (emailExists.isPresent()) {
            if (!emailExists.get().isActive()) {
                throw new CommonExceptionHandler.BadRequestException(
                        "Inactive User."
                );
            }
            throw new CommonExceptionHandler.BadRequestException(
                    "User with this email already exists."
            );
        }
        return true;
    }

    @Override
    @Transactional
    public User updateUser(CreateUserDto updates, String orgId, String userId) {
        String schema = TenantUtil.getCurrentTenant();
        UserEntity existingUser = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingUser.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized");
        }

        List<Long> userLocation = locationAdapter.findUserLocationByUserId(userId)
                .stream()
                .map(location -> location.getLocation().getLocationId())
                .toList();

        log.info("User location: {}", userLocation);

        List<Long> userGroup = userAdapter.findUserGroupByUserId(userId)
                .stream()
                .map(group -> group.getGroup().getGroupId())
                .toList();
        log.info("User group: {}", userGroup);

        UserDto userDto = updates.getUser();
        List<Long> location = userDto.getLocationId();
        List<Long> group = userDto.getGroupId();

        if (userDto.getRoleId() != null) {
            existingUser.setRole(organizationAdapter.findRoleById(userDto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found")));
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getUserName() != null) {
            existingUser.setUserName(userDto.getUserName());
        }
        if (userDto.getMobileNumber() != null) {
            existingUser.setMobileNumber(userDto.getMobileNumber());
        }
        if (userDto.isRegisterUser()) {
            existingUser.setRegisterUser(userDto.isRegisterUser());
        }
        if (userDto.getWorkSchedule() != null) {
            existingUser.setWorkSchedule(workScheduleAdapter.findByScheduleId(userDto.getWorkSchedule(), orgId));
        }
        if (location != null) {
            Set<Long> toDelete = new HashSet<>(userLocation);
            location.forEach(toDelete::remove);
            Set<Long> toInsert = new HashSet<>(location);
            userLocation.forEach(toInsert::remove);
            if (!toDelete.isEmpty()) {
                locationAdapter.deleteUserLocationByUserId(userId, toDelete);
                log.info("Deleted user location: {}", toDelete);
            }
            if (!toInsert.isEmpty()) {
                List<UserLocationEntity> newEntities = toInsert.stream()
                        .map(locations ->
                        {
                            UserLocationEntity userLocationEntity = new UserLocationEntity();
                            userLocationEntity.setUser(existingUser);
                            userLocationEntity.setLocation(locationRepository.findById(locations)
                                    .orElseThrow(() -> new RuntimeException("Location not found")));
                            return userLocationEntity;
                        })
                        .toList();
                locationAdapter.updateUserLocationByUserId(newEntities);
                log.info("Added user location: {}", toInsert);
            }
        }
        if (group != null) {
            Set<Long> toDelete = new HashSet<>(userGroup);
            group.forEach(toDelete::remove);
            Set<Long> toInsert = new HashSet<>(group);
            userGroup.forEach(toInsert::remove);
            if (!toDelete.isEmpty()) {
                userAdapter.deleteUserGroupByUserId(userId, toDelete);
                log.info("Deleted user group: {}", toDelete);
            }
            if (!toInsert.isEmpty()) {
                List<UserGroupEntity> newEntities = toInsert.stream()
                        .map(groups ->
                        {
                            UserGroupEntity userGroupEntity = new UserGroupEntity();
                            userGroupEntity.setUser(existingUser);
                            userGroupEntity.setGroup(groupRepository.findById(groups)
                                    .orElseThrow(() -> new RuntimeException("Group not found")));
                            return userGroupEntity;
                        })
                        .toList();
                userAdapter.updateUserGroupByUserId(newEntities);
            }
        }
        if (userDto.getDateOfJoining() != null) {
            existingUser.setDateOfJoining(userDto.getDateOfJoining());
        }
        String newRequesterId = updates.getUser().getRequestApproverId();

        if (newRequesterId == null || newRequesterId.isBlank()) {
            if (existingUser.getRequestApproverId() != null) {
                existingUser.setRequestApproverId(null);
            }
        } else {
            List<String> oneUserList = List.of(userId);
            validateApprover(newRequesterId, oneUserList);
            existingUser.setRequestApproverId(newRequesterId);
        }

        updateUserCalendar(existingUser, updates.getUser().getCalendarId());

        String key = organizationCacheService.getPrivilegeKey(PrivilegeConstants.HAVE_SECONDARY_DETAILS);
        boolean hasSecondaryDetailsPrivilege = rolePrivilegeHelper.roleHasPrivilege(existingUser.getRole().getName(), key);
        SecondaryDetailsEntity savedSecondaryUser = null;
        if (hasSecondaryDetailsPrivilege) {
            SecondaryDetailsDto secondaryDetails = updates.getSecondaryDetails();

            if (secondaryDetails != null) {
                Optional<SecondaryDetailsEntity> existingSecondaryUser = userAdapter.findSecondaryUserById(userId);
                SecondaryDetailsEntity secondaryUser;

                if (existingSecondaryUser.isPresent()) {
                    secondaryUser = existingSecondaryUser.get();
                    log.info("Updating existing secondary details for userId: {}", existingUser.getUserId());
                } else {
                    secondaryUser = new SecondaryDetailsEntity();
                    secondaryUser.setId(idGenerationService.generateNextSecondaryUserId(orgId));
                    secondaryUser.setUser(existingUser);
                    log.info("Creating new secondary details row for userId: {}", existingUser.getUserId());
                }

                if (secondaryDetails.getUserName() != null) {
                    secondaryUser.setUserName(secondaryDetails.getUserName());
                }
                if (secondaryDetails.getMobile() != null) {
                    secondaryUser.setMobile(secondaryDetails.getMobile());
                }
                if (secondaryDetails.getEmail() != null) {
                    secondaryUser.setEmail(TextUtil.trim(secondaryDetails.getEmail()));
                }
                if (secondaryDetails.getRelation() != null) {
                    secondaryUser.setRelation(secondaryDetails.getRelation());
                }
                savedSecondaryUser = userAdapter.saveSecondaryDetails(secondaryUser);
            } else {
                log.info("No secondary details provided. Skipping secondary details update.");
            }
            if (secondaryDetails != null) {
                log.info("Processing secondary user mapping in public.user_map table...");
                log.info("User mobile number : {}", secondaryDetails.getMobile());
                Optional<UserSchemaMappingEntity> existingMappingOpt =
                        userAdapter.findUserByMobileAndOrgId(secondaryDetails.getMobile(), orgId);
                if (existingMappingOpt.isEmpty()) {
                    UserSchemaMappingEntity mapping = organizationEntityMapper.toSchema(
                            null,
                            secondaryDetails.getMobile(),
                            orgId,
                            TenantContext.getCurrentTenant()
                    );
                    UserSchemaMappingEntity savedMapping = userAdapter.update(mapping);
                }
            }
        }
        userAdapter.updateUser(existingUser);
        userAdapter.flush();
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new UserEvent(orgId, authHelper.getSchema()));
                log.info("UserCacheReloadEvent published after Update User for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish UserCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache Update User reload.");
        }
        return userEntityMapper.toMiddleware(existingUser);
    }

    @Override
    public List<UserResponseDto> getUsers(String orgId, String role) {
        return fetchActiveUsers(orgId, role);
    }

    private List<UserResponseDto> fetchActiveUsers(String orgId, String role) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Fetching active users | tenant: {}", schema);
        String userCacheKey = cacheKeyUtil.getMemberKey(orgId, schema);
        String roleField = role.toLowerCase();
        log.info("Fetching from cache key: {}, field: {}", userCacheKey, roleField);
        try {
            //Try fetching from Redis first
            if (redisTemplate != null) {
                Object cachedObj = redisTemplate.opsForHash().get(userCacheKey, roleField);

                if (cachedObj instanceof List<?> cachedList && !cachedList.isEmpty()) {
                    log.info("Cache hit | orgId={}, role={}", orgId, role);
                    return (List<UserResponseDto>) cachedList;
                } else {
                    log.warn("Cache miss or empty list | orgId={}, role={}", orgId, role);
                }
            } else {
                log.warn("RedisTemplate is null, skipping cache fetch for key: {}, field: {}", userCacheKey, roleField);
            }
            // Cache miss OR empty cache → Fetch from DB & reload cache
            log.info("Loading fresh users from DB for orgId={}, role={}", orgId, role);
            Map<String, List<UserResponseDto>> roleMap = userCacheService.loadAllUsers(orgId, schema).get();
            List<UserResponseDto> freshUsers = roleMap.get(role.toUpperCase());
            if (freshUsers != null && !freshUsers.isEmpty()) {
                log.info("Returning fresh users from DB for orgId={}, role={}", orgId, role);
                return freshUsers;
            }
        } catch (Exception e) {
            log.error("Failed to fetch users | orgId={}, role={}. Error: {}", orgId, role, e.getMessage(), e);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found for this organization");
    }

    @Override
    @Transactional
    public void deleteUsers(String orgId, List<String> userIds, String userNameFromToken, String comments) {
        if (userIds == null || userIds.isEmpty()) return;
        String schema = TenantUtil.getCurrentTenant();
        userAdapter.deactivateUsersByIds(userIds, orgId);
        log.info("Deactivated users in bulk");
        List<UserHistoryEntity> historyEntities = userIds.stream()
                .map(id -> {
                    String commentLog = "Inactivated By " + userNameFromToken + " - " + comments;
                    return userEntityMapper.toInactiveUserEntity(id, commentLog);
                })
                .toList();

        if (!historyEntities.isEmpty()) {
            userAdapter.saveAllUserHistories(historyEntities);
            log.info("Saved {} user history records in batch", historyEntities.size());
        }
        CompletableFuture.runAsync(() ->
                userIds.parallelStream().forEach(userId -> {
                    try {
                        userAdapter.deleteUserFace(userId);
                    } catch (Exception e) {
                        log.error("Failed to delete face for user {}: {}", userId, e.getMessage());
                    }
                })
        );
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new UserEvent(orgId, authHelper.getSchema()));
                log.info("UserCacheReloadEvent published after after bulk inactivation of a user for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish UserCacheReloadEvent for orgId={}", orgId, e);
            }
        }  else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload after bulk inactivation.");
        }
        log.info("Deleted users successfully: {}", userIds.size());
    }

    @Override
    @Transactional
    public AddGroup createGroup(AddGroup groupMiddleware, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        if (userAdapter.findByGroup(groupMiddleware.getGroupName(), orgId)) {
            throw new DataIntegrityViolationException("Group '" + groupMiddleware.getGroupName() + "' already exists in this organization");
        }

        GroupEntity entity = userEntityMapper.toEntity(groupMiddleware);

        OrganizationEntity orgEntity = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + orgId));
        entity.setOrganizationEntity(orgEntity);

        if (groupMiddleware.getLocationId() != null) {
            LocationEntity locationEntity = locationRepository.findById(groupMiddleware.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + groupMiddleware.getLocationId()));
            entity.setLocationEntity(locationEntity);
        }

        if (groupMiddleware.getWorkScheduleId() == null || groupMiddleware.getWorkScheduleId().isEmpty()) {
            WorkScheduleEntity defaultWs = workScheduleAdapter.findDefaultActiveSchedule(orgId);
            log.info("Default schedule:{}", defaultWs);
            entity.setWorkSchedule(defaultWs);
        } else {
            WorkScheduleEntity ws = workScheduleAdapter.findByScheduleId(groupMiddleware.getWorkScheduleId(), orgId);
            entity.setWorkSchedule(ws);
        }

        GroupEntity savedEntity = userAdapter.saveGroup(entity);

        for (String id : groupMiddleware.getSupervisorsId()) {
            UserEntity user = userAdapter.findById(id).orElseThrow(() -> new UsernameNotFoundException("User ID " + id + " not found."));

            createUserGroup(new UserGroup(savedEntity.getGroupId(), id, groupMiddleware.getType()), orgId);

        }
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new GroupEvent(orgId, authHelper.getSchema()));
                log.info("GroupCacheReloadEvent published after Group Creation for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish GroupCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache create group reload.");
        }
        return userEntityMapper.toGroupMiddleware(savedEntity);
    }

    @Override
    @Transactional
    public ApiResponse addUserToGroup(AddMember addMemberMiddleware, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        List<String> userIds = addMemberMiddleware.getUserId();
        List<String> addedUserNames = new ArrayList<>();
        List<String> alreadyExistsUsers = new ArrayList<>();
        if (userIds == null || userIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User IDs must not be null or empty"
            );
        }

        long existingCount = userAdapter.countExistingUsers(userIds);

        if (existingCount != userIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found with ID: "
            );
        }

        List<UserEntity> users = userAdapter.findAllById(userIds);

        List<UserGroupEntity> existingMappings =
                userAdapter.findByGroupIdAndUserIdIn(addMemberMiddleware.getGroupId(), userIds);
        Map<String, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getUserId, Function.identity()));

        Set<String> existingUserIds = existingMappings.stream()
                .map(ug -> ug.getUser().getUserId())
                .collect(Collectors.toSet());

        for (String id : userIds) {

            UserEntity user = userMap.get(id);
            if (user == null) continue;

            if (existingUserIds.contains(id)) {
                alreadyExistsUsers.add(user.getUserName());
                continue;
            }

            createUserGroup(
                    new UserGroup(addMemberMiddleware.getGroupId(), id, addMemberMiddleware.getType()),
                    orgId
            );
            addedUserNames.add(user.getUserName());
        }


            String addedMessage = addedUserNames.isEmpty()
                ? ""
                : "Successfully added users: " + String.join(", ", addedUserNames) + ".";

        String existsMessage = alreadyExistsUsers.isEmpty()
                ? ""
                : "These users were already in the group: " + String.join(", ", alreadyExistsUsers) + ".";

        String finalMessage = addedMessage + existsMessage;
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new GroupEvent(orgId, authHelper.getSchema()));
                log.info("GroupCacheReloadEvent published after adding user to group for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish GroupCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache add User to group reload.");
        }
        return new ApiResponse<>(200, finalMessage, null);
    }

    @Override
    public UserGroup createUserGroup(UserGroup userGroupMiddleware, String orgId) {
        List<UserGroupEntity> existing = userAdapter.findByUserIdAndGroupId(
                userGroupMiddleware.getUserId(),
                userGroupMiddleware.getGroupId()
        );
        if (!existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user is already assigned to this group more than once.");
        }

        UserGroupEntity entity = userEntityMapper.toEntity(userGroupMiddleware);
        UserGroupEntity savedEntity = null;
        savedEntity = userAdapter.saveUserGroup(entity);

        return userEntityMapper.toMiddleware(savedEntity);
    }

    @Transactional
    @Override
    public ApiResponse<?> updateGroupDetails(AddGroupDto addGroupDto, Long groupId, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        AddGroup addGroup = userDtoMapper.toMiddleware(addGroupDto);

        List<String> conflictMessages = new ArrayList<>();
        List<String> addedSupervisors = new ArrayList<>();

        GroupEntity existingGroup = userAdapter.findByGroupId(groupId).orElse(null);
        if (existingGroup == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found with ID: " + groupId);
        }

        boolean groupNameChanged = false;
        if (addGroup.getGroupName() != null && !addGroup.getGroupName().equals(existingGroup.getGroupName())) {
            boolean nameExists = userAdapter.existsGroupNameInOrganization(addGroup.getGroupName(), orgId, groupId);
            if (nameExists) {
                conflictMessages.add("Group name already exists");
            } else {
                existingGroup.setGroupName(addGroup.getGroupName());
                groupNameChanged = true;
            }
        }

        if (addGroup.getLocationId() != null) {
            LocationEntity locationEntity = locationAdapter.findLocationById(addGroup.getLocationId(), orgId);
            if (locationEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found with ID: " + addGroup.getLocationId());
            }
            existingGroup.setLocationEntity(locationEntity);
        }

        if (addGroup.getWorkScheduleId() != null) {
            WorkScheduleEntity workScheduleEntity = workScheduleAdapter.findByScheduleId(addGroup.getWorkScheduleId(), orgId);
            if (workScheduleEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkSchedule Not found with ID: " + addGroup.getWorkScheduleId());
            }
            existingGroup.setWorkSchedule(workScheduleEntity);
        }

        userAdapter.saveGroup(existingGroup);

        List<String> existingSupervisorIds = userAdapter.findSupervisorIdsByGroupId(groupId);
        Set<String> newSupervisorIds = addGroup.getSupervisorsId() != null
                ? new HashSet<>(addGroup.getSupervisorsId())
                : new HashSet<>();

        for (String existingSupervisorId : existingSupervisorIds) {
            if (!newSupervisorIds.contains(existingSupervisorId)) {
                userAdapter.deleteSupervisorsByGroupId(groupId, existingSupervisorId);
            }
        }

        List<String> existingMemberIds = userAdapter.findMemberIdsByGroupId(groupId);
        for (String supervisorId : newSupervisorIds) {
            if (existingMemberIds.contains(supervisorId)) {
                UserEntity supervisorUser = userAdapter.findById(supervisorId).orElse(null);
                if (supervisorUser != null) {
                    conflictMessages.add("User " + supervisorUser.getUserName() + " is already a member in this group");
                } else {
                    conflictMessages.add("User ID " + supervisorId + " is already a member in this group");
                }
            } else {
                userAdapter.deleteSupervisorsByGroupId(groupId, supervisorId);

                UserGroupEntity supervisorEntry = new UserGroupEntity();
                supervisorEntry.setGroup(existingGroup);
                supervisorEntry.setUser(new UserEntity(supervisorId));
                supervisorEntry.setType("Supervisor");
                userAdapter.saveUserGroup(supervisorEntry);

                UserEntity supervisorUser = userAdapter.findById(supervisorId).orElse(null);
                if (supervisorUser != null) {
                    addedSupervisors.add(supervisorUser.getUserName());
                } else {
                    addedSupervisors.add("UserID: " + supervisorId);
                }
            }
        }

        String conflictMessage = conflictMessages.isEmpty()
                ? ""
                : String.join(", ", conflictMessages) + ".";

        String finalMessage = conflictMessage.trim();

        if (!conflictMessages.isEmpty()) {
            return new ApiResponse<>(HttpStatus.CONFLICT.value(), finalMessage, Collections.emptyList());
        }
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new GroupEvent(orgId, authHelper.getSchema()));
                log.info("GroupCacheReloadEvent published after Group Details Update for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish GroupCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache update group reload.");
        }
        return new ApiResponse<>(HttpStatus.OK.value(), "Group updated Successfully.", Collections.emptyList());
    }

    @Override
    public List<UserNameSuggestionDto> searchUsernames(String keywords) {
        if (keywords == null || keywords.trim().length() < 3) {
            throw new RuntimeException("Minimum 3 characters required");
        }
        List<UserNameSuggestionDto> results = userAdapter.searchUserNamesContaining(keywords);
        return results;
    }

    @Override
    public UserProfileResponseDto getUserProfile(String orgId, String userId) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current User Profile tenant:{}", schema);
        String redisKey = cacheKeyUtil.getProfileKey(orgId, schema);
        try {
            if (redisTemplate != null) {
                UserProfileResponseDto cachedData = (UserProfileResponseDto) redisTemplate.opsForHash().get(redisKey, userId);
                if (cachedData != null) {
                    log.info("Cache hit for userId {} in orgId {}", userId, orgId);
                    return cachedData;
                } else {
                    log.info("Cache miss for userId {}, loading from DB...", userId);
                    Map<String, UserProfileResponseDto> loadedProfiles = userCacheService.loadUsersProfile(orgId, schema).get();

                    UserProfileResponseDto response = loadedProfiles.get(userId);
                    if (response != null) {
                        log.info("Fallback DB profile returned for userId {}", userId);
                        return response;
                    } else {
                        log.warn("User profile not found even in DB for userId {}", userId);
                        return null;
                    }
                }
            } else {
                log.warn("Redis disabled, directly loading userId {} from DB", userId);
                return loadSingleUserProfile(orgId, userId);
            }

        } catch (Exception e) {
            log.error("Error in user profile fetch logic for userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    public UserProfileResponseDto loadSingleUserProfile(String orgId, String userId) {

        // Step 1 — Load single user projection the same way as list
        List<UserProjection> rows = userAdapter.findUserByUserId(userId);

        if (rows == null || rows.isEmpty()) {
            log.warn("User not found for userId {} in orgId {}", userId, orgId);
            return null;
        }

        // Step 2 — Merge like global merge logic
        Map<String, UserResponseDto> merged = userMergeUtil.mergeUserRecords(rows);

        UserResponseDto user = merged.values().iterator().next();

        List<UserLocationEntity> userLocations = locationAdapter.findByUser_UserId(user.getUserId());
        List<LocationDto> locationDtos;

        if (userLocations.isEmpty()) {
            log.warn("No locations found for userId {}", user.getUserId());
            locationDtos = Collections.emptyList();
        } else {
            locationDtos = userLocations.stream()
                    .filter(ul -> ul.getLocation() != null)
                    .map(ul -> locationDtoMapper.toDto(ul.getLocation()))
                    .toList();
        }

        List<UserGroupEntity> userGroups = userAdapter.findUserByOrganizationIdAndUserId(orgId, user.getUserId());
        List<UserGroupProfileDto> groupDtos = userGroups.isEmpty()
                ? Collections.emptyList()
                : userGroups.stream().map(userDtoMapper::toGroupsDto).toList();

        // Step 5 — Parent (Student only)
        List<ParentDto> parentDtos = new ArrayList<>();
        if (user.getSecondaryDetails() != null) {
            SecondaryDetailsDto sec = user.getSecondaryDetails();
            parentDtos.add(new ParentDto(
                    null,
                    sec.getUserName(),
                    sec.getEmail(),
                    sec.getMobile()
            ));
        }

        // Step 6 — Single policy for profile (first one)
        UserPolicyDto singlePolicy =
                user.getPolicies() != null && !user.getPolicies().isEmpty()
                        ? user.getPolicies().getFirst()
                        : null;

        // Step 7 — Build Profile Response
        return new UserProfileResponseDto(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRoleName(),
                user.getDateOfJoining(),
                locationDtos,
                groupDtos,
                user.getOrganizationName() != null ? user.getOrganizationName() : "-",
                user.getScheduleName(),
                user.getOrgType() != null ? user.getOrgType() : "-",
                user.getCalendarName(),
                user.getRequestApproverName(),
                user.getPayrollName(),
                (List<UserPolicyDto>) singlePolicy,
                parentDtos
        );
    }

    public List<GroupResponseDto> getAllGroups(String orgId, String userId) throws JsonProcessingException {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current Group tenant:{}", schema);
        String cacheGroupkey = cacheKeyUtil.getAllGroupsKey(orgId, schema);
        String cacheSupervisedGroupKey = cacheKeyUtil.getSupervisedGroupsKey(orgId, schema);

        UserEntity currentUser = userAdapter.getUserById(userId);
        String roleName = currentUser.getRole().getName().toUpperCase();

        String canSeeAllGroupskey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_GROUPS);
        boolean canSeeAllGroups = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeAllGroupskey);
        String canSeeSupervisingGroupsKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_SUPERVISING_GROUPS);
        boolean canSeeSupervisingGroups = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeSupervisingGroupsKey);

        log.info("canSeeAllGroups={}, canSeeSupervisingGroups={}", canSeeAllGroups, canSeeSupervisingGroups);
        try {
            if (canSeeAllGroups && redisTemplate != null) {
                Map<Object, Object> allGroups = redisTemplate.opsForHash().entries(cacheGroupkey);
                if (!allGroups.isEmpty()) {
                    log.info("Cache hit for All groups key");
                    return allGroups.values().stream()
                            .map(obj -> (GroupResponseDto) obj)
                            .toList();
                }
                log.warn("Cache miss for all groups. Loading from DB...");
            } else if (canSeeAllGroups) {
                log.warn("RedisTemplate is null, skipping cache fetch for all groups.");
            } else if (canSeeSupervisingGroups && redisTemplate != null) {
                Object groupIdSetObj = redisTemplate.opsForHash().get(cacheSupervisedGroupKey, String.valueOf(userId));
                if (groupIdSetObj instanceof Set<?> groupIdSet && !groupIdSet.isEmpty()) {
                    List<GroupResponseDto> result = new ArrayList<>();
                    for (Object gid : groupIdSet) {
                        GroupResponseDto group = (GroupResponseDto) redisTemplate.opsForHash().get(cacheGroupkey, String.valueOf(gid));
                        if (group != null) result.add(group);
                    }
                    if (!result.isEmpty()) {
                        log.info("Fetched {} groups from supervisor cache for userId={}", result.size(), userId);
                        return result;
                    }
                    log.warn("Supervisor group cache empty or group details not found. Loading from DB...");
                }
            } else if (canSeeSupervisingGroups) {
                log.warn("RedisTemplate is null, skipping supervisor cache fetch.");
            }

            log.info("Loading from DB as cache is missing for orgId={}, userId={}", orgId, userId);
            try {
                List<GroupResponseDto> groupsFromDb = userCacheService
                        .loadGroupsCache(orgId, schema)
                        .get();

                if (canSeeAllGroups) {
                    return groupsFromDb;
                } else if (canSeeSupervisingGroups) {
                    return groupsFromDb.stream()
                            .filter(group -> group.getMembersDetails().stream()
                                    .anyMatch(m -> m.getUserId().equals(userId) && "SUPERVISOR".equalsIgnoreCase(m.getType())))
                            .toList();
                } else {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to view any groups");
                }

            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to load group cache: {}", e.getMessage(), e);
                throw new RuntimeException("Cache loading failed", e);
            }

        } catch (Exception e) {
            log.error("Failed to get groups from cache or DB: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load groups");
        }
    }

    @Override
    public boolean updateUserGroupType(UserGroup userGroup) {
        Map<String, String> typeMap = Map.of(
                "m", MemberType.MEMBER.getValue(),
                "s", MemberType.SUPERVISOR.getValue()
        );

        String type = typeMap.entrySet().stream()
                .filter(entry -> userGroup.getType().toLowerCase().startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid type. Must start with 'm' or 's'."));

        int updatedCount = userAdapter.updateUserGroupType(userGroup.getUserId(), userGroup.getGroupId(), type);
        return updatedCount > 0;
    }

    @Override
    @Transactional
    public void deleteMember(DeleteMemberModel model, String orgId) {
        Long groupId = model.getGroupId();
        List<String> memberId = model.getMemberId();
        String schema = TenantUtil.getCurrentTenant();
        userAdapter.deleteMember(groupId, memberId);
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new GroupEvent(orgId, schema));
                log.info("GroupCacheReloadEvent published after Group Member Deletion");
            } catch (Exception e) {
                log.error("Failed to publish GroupCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache delete member from group reload.");
        }
    }

    @Override
    @Transactional
    public void deleteGroups(GroupBulkDeleteModel model, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        List<Long> groupIds = model.getGroupIds();
        List<Long> existingGroupIds = groupRepository.findExistingGroupIds(groupIds, orgId);
        if (existingGroupIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No valid groups found or access denied");
        }
        userAdapter.deleteByGroupIds(existingGroupIds);
        userAdapter.deleteGroups(existingGroupIds, orgId);
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new GroupEvent(orgId, schema));
                log.info("GroupCacheReloadEvent published after multiple group deletions");
            } catch (Exception e) {
                log.error("Failed to publish UserCacheReloadEvent for orgId={}", orgId, e);
            }
        }
    }

    @Override
    public List<User> getMembers(String orgId, Long roleId) {
        if (roleId != null) {
            return userAdapter.getMembers(orgId, roleId).stream()
                    .map(userEntityMapper::toMiddleware).toList();
        } else {
            int studentHierarchyLevel = UserRole.STUDENT.getHierarchyLevel();
            int superadminHierarchyLevel = UserRole.SUPERADMIN.getHierarchyLevel();
            List<UserRole> higherRoles = Arrays.stream(UserRole.values())
                    .filter(r -> r.getHierarchyLevel() < studentHierarchyLevel && r.getHierarchyLevel() > superadminHierarchyLevel)
                    .toList();
            List<Integer> higherRoleIds = higherRoles.stream()
                    .map(roles -> roles.getHierarchyLevel())
                    .toList();
            return userAdapter.getMembersByRole(orgId, higherRoleIds).stream()
                    .map(userEntityMapper::toMiddleware).toList();
        }
    }

    @Override
    public List<GroupDto> getUserGroups(String userId, String role, String orgId) {
        String roleName = role.replace("ROLE_", "");
        if (RoleName.SUPERADMIN.getRoleName().equalsIgnoreCase(roleName)) {
            List<GroupDto> Allgroup = userAdapter.getAllgroups(orgId);
            return Allgroup;
        }
        List<GroupDto> group = userAdapter.getUserGroups(userId, orgId);
        return group;
    }

    @Override
    public List<Map<String, Object>> getGroupMembers(Long groupId, String orgId, LocalDate date, String userIdFromToken) {
        List<UserGroupEntity> groupEntity = userAdapter.getGroupMembersByGroupId(groupId, orgId);

        List<String> memberIds = groupEntity.stream()
                .map(ug -> ug.getUser().getUserId())
                .filter(id -> !id.equals(userIdFromToken))
                .toList();

        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> userType = groupEntity.stream()
                .collect(Collectors.toMap(
                        ug -> ug.getUser().getUserId(),
                        UserGroupEntity::getType
                ));

        List<UserEntity> groupUsers = userAdapter.getUsersByIds(memberIds, orgId);

        List<TimesheetEntity> latestLogs = timesheetAdapter.getLatestLogsByTimesheetIds(memberIds, orgId, date);
        Map<String, TimesheetEntity> latestLogsMap = latestLogs.stream()
                .collect(Collectors.toMap(te -> te.getUser().getUserId(), Function.identity()));

        List<Map<String, Object>> userDetailsList = groupUsers.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getUserId());
                    map.put("name", user.getUserName());
                    map.put("role", user.getRole().getName());
                    map.put("isRegistered", user.isRegisterUser());
                    map.put("type", userType.get(user.getUserId()));
                    TimesheetEntity timesheet = latestLogsMap.get(user.getUserId());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

                    if (timesheet != null) {
                        LocalTime clockIn = timesheet.getFirstClockIn();
                        LocalTime clockOut = timesheet.getLastClockOut();

                        map.put("firstClockIn", clockIn != null ? clockIn.format(formatter) : null);
                        map.put("lastClockOut", clockOut != null ? clockOut.format(formatter) : null);
                    } else {
                        map.put("firstClockIn", null);
                        map.put("lastClockOut", null);
                    }
                    return map;
                })
                .toList();

        return userDetailsList;
    }

    @Override
    public List<UserNameSuggestionDto> getGroupUsers(List<Long> groupIds, String orgId, String loggedInUserId, String role) {
        if (groupIds == null || groupIds.isEmpty()) {
            int hierarchyLevel = UserRole.getLevel(role);
            List<UserNameSuggestionDto> allUsers = userAdapter.getAllActiveUsers(orgId, hierarchyLevel);

            return allUsers.stream()
                    .filter(user -> !user.getUserId().equals(loggedInUserId))  // Exclude logged-in user
                    .map(userDto -> new UserNameSuggestionDto(userDto.getUserId(), userDto.getUserName()))  // Map to UserNameSuggestionDto
                    .collect(Collectors.toList());
        }
        log.info("Role: {}", role);
        if (RoleName.SUPERADMIN.getRoleName().equalsIgnoreCase(role)) {
            log.info("SuperAdmin role");
            List<UserNameSuggestionDto> allUsers = userAdapter.getAllGroupUsers(groupIds, orgId);
            List<UserNameSuggestionDto> groupMembers = allUsers.stream()
                    .collect(Collectors.toMap(
                            UserNameSuggestionDto::getUserId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .toList();
            return groupMembers;
        }
        log.info("Role not used" + role);
        List<UserGroupEntity> groupEntity = userAdapter.getGroupUsersByGroupId(groupIds, orgId);

        List<String> memberIds = groupEntity.stream()
                .filter(ug -> ug.getType().equalsIgnoreCase("Member"))
                .map(ug -> ug.getUser().getUserId())
                .filter(id -> !id.equals(loggedInUserId))
                .distinct()
                .collect(Collectors.toList());

        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserEntity> groupUsers = userAdapter.getUsersByIds(memberIds, orgId);
        return groupUsers.stream()
                .map(userEntity -> new UserNameSuggestionDto(userEntity.getUserId(), userEntity.getUserName()))  // Map to UserNameSuggestionDto
                .collect(Collectors.toList());
    }

    @Override
    public String findGroupName(Long requestedGroupId) {
        return groupRepository.findGroupNameByGroupId(requestedGroupId);
    }

    @Override
    public List<UserResponseDto> getInactiveUsers(String orgId, String role) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current location tenant:{}", schema);
        String userCacheKey = cacheKeyUtil.getInactiveMemberKey(orgId, schema);
        String roleField = role.toLowerCase();
        log.info("roleField:{}", roleField);
        try {
            if (redisTemplate != null) {
                Object cachedObj = redisTemplate.opsForHash().get(userCacheKey, roleField);
                if (cachedObj != null) {
                    log.info("Cache hit for orgId={}, role={}", orgId, role);
                    return (List<UserResponseDto>) cachedObj;
                }
            } else {
                log.warn("RedisTemplate is null, skipping cache fetch for key: {}, field: {}", userCacheKey, roleField);
            }

            log.info("Cache miss for orgId={}, role={}. Loading from DB...", orgId, role);
            Map<String, List<UserResponseDto>> roleMap = userCacheService.loadAllInactiveUsers(orgId, schema).get();
            log.info("rolemap:{}", roleMap);
            String loggedRole = roleField.toUpperCase();
            List<UserResponseDto> fallbackList = roleMap.get(loggedRole);
            if (fallbackList != null && !fallbackList.isEmpty()) {
                log.info("Returning response from DB for All Users");
                return fallbackList;
            }

        } catch (Exception e) {
            log.error("Failed to fetch users for orgId={}, role={}. Error: {}", orgId, role, e.getMessage(), e);
        }

        throw new ResponseStatusException(HttpStatus.OK, "No Inactive Users found for this organization");
    }

    @Override
    public List<EditUserDto> updateIsActive(EditUser editUser, String orgId, String userNameFromToken) {
        String schema = TenantUtil.getCurrentTenant();
        List<String> userIds = editUser.getUserId();
        if (userIds == null || userIds.isEmpty()) {
            throw new RuntimeException("No userIds provided");
        }
        List<UserEntity> userEntityList = new ArrayList<>();
        for (String id : userIds) {
            UserEntity userEntities = userAdapter.findUserByOrgIdAndUserId(orgId, id);
            if (userEntities == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No user found for the provided userId");
            }

            userEntities.setActive(editUser.isActive());
            userEntityList.add(userEntities);

            String Comments = "Activated By " + userNameFromToken + "- " + editUser.getComments();
            UserHistoryEntity userHistoryEntity = userEntityMapper.toActiveUserEntity(id, Comments);
            log.info("Save User History Log");
            userAdapter.saveUserHistory(userHistoryEntity);
            log.info("Saved User log History");
        }
        List<UserEntity> user = userAdapter.save(userEntityList);
        List<EditUserDto> dto = userDtoMapper.toDto(user);
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new InactiveUserEvent(orgId, schema));
                log.info("InactiveUserCacheReloadEvent published after Active update");
            } catch (Exception e) {
                log.error("Failed to publish InactiveUserCacheReloadEvent for orgId={}", orgId, e);
            }
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache update Inactive User reload.");
        }
        return dto;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ApiResponse createSuperAdminUser(Organization organization, String organizationId, String schemaName) {
        try {
            log.info("Creating SuperAdmin in schema: {} for OrgId {}", schemaName, organizationId);

            if (isBlank(organization.getUserName()) ||
                    isBlank(organization.getMobile()) ||
                    isBlank(organization.getEmail())) {
                throw new CommonExceptionHandler.BadRequestException("Mandatory fields must not be null");
            }
            log.info("find user by mobile number");
            userAdapter.findByMobileNumber(organization.getMobile()).ifPresent(user -> {
                throw new CommonExceptionHandler.BadRequestException("Mobile number already in use.");
            });
            log.info("find user by email");
            userAdapter.findByEmail(organization.getEmail()).ifPresent(user -> {
                throw new CommonExceptionHandler.BadRequestException("Email already in use.");
            });
            log.info("find user by role");
            RoleEntity role = roleRepository.findById((long) UserRole.SUPERADMIN.getHierarchyLevel())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            log.info("Role founded");
            UserEntity entity = new UserEntity();
            entity.setUserName(organization.getUserName());
            entity.setEmail(organization.getEmail());
            entity.setMobileNumber(organization.getMobile());
            entity.setOrganizationId(organizationId);

            String customUserId = idGenerationService.generateNextUserId(organizationId);
            entity.setUserId(customUserId);
            entity.setRole(role);
            entity.setDateOfJoining(LocalDate.now(ZoneId.of("Asia/Kolkata"))
            );

            String defaultPassword = PasswordUtil.generateDefaultPassword();
            entity.setPassword(PasswordUtil.encryptPassword(defaultPassword));
            entity.setDefaultPassword(true);
            entity.setActive(true);
            entity.setCreatedAt(LocalDateTime.now());

            log.info("Save superadmin");
            UserEntity savedUser = userAdapter.saveUser(entity);

            String planId = userAdapter.findByPlan();
            SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
            subscriptionEntity.setSubId(idGenerationService.generateNextSubscriptionId(organizationId));
            subscriptionEntity.setOrgId(organizationId);
            subscriptionEntity.setPlanId(planId);
            subscriptionEntity.setSchemaName(schemaName);
            subscriptionEntity.setStatus(OrganizationStatusEnum.ACTIVE.getDisplayValue());
            subscriptionEntity.setStartDate(LocalDateTime.now());
            subscriptionEntity.setEndDate(LocalDateTime.now().plusDays(30));
            subscriptionEntity.setSubscribedUsers(subscribedUsers);
            log.info("Save subscription for created Organization");
            SubscriptionEntity saveSubscription = userAdapter.saveSubscription(subscriptionEntity);

            emailHelper.sendAccountCreationEmail(
                    savedUser.getEmail(), savedUser.getUserName(), defaultPassword, true, savedUser.getRole().getRoleId()
            );

            log.info("SuperAdmin created successfully in schema.users for org {}", organizationId);
            return new ApiResponse(201, "SuperAdmin created successfully", userEntityMapper.toMiddleware(savedUser));

        } catch (Exception e) {
            log.error("Error creating SuperAdmin user: {}", e.getMessage(), e);
            throw new CommonExceptionHandler.InternalServerException("Failed to create SuperAdmin user. " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<UserHistoryResponseDto>> getUserHistoryLog(String userId) {
        log.info("Fetching user history for userId: {}", userId);
        List<UserHistoryEntity> responseDtos = userAdapter.getUserHistoryLog(userId);
        log.info("Fetched {} history records for userId {}", responseDtos.size(), userId);
        if (responseDtos.isEmpty()) {
            return new ApiResponse<>(404, "No history found for this user", null);
        }
        List<UserHistoryResponseDto> responseDtoList = userEntityMapper.toHistoryDto(responseDtos);
        return new ApiResponse<>(200, "User History Log return successfully", responseDtoList);
    }

    @Override
    public ResponseEntity<Resource> downloadSampleFile() {
        try {
            Resource resource = new ClassPathResource("templates/sampleReports/Sample_Template.csv");

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Sample_Template.csv");
            return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public BulkRoleUpdateModel updateMultipleUserRoles(BulkRoleUpdateModel model, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        Long roleId = model.getRoleId();
        List<String> userIds = model.getUserIds();
        RoleEntity newRole = userAdapter.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        List<UserEntity> users = userAdapter.findByUserId(userIds);
        int updatedCount = 0;
        int skippedCount = 0;
        List<UserEntity> updatedUsers = new ArrayList<>();
        log.info("UserIds from request: {}", model.getUserIds());
        for (UserEntity user : users) {
            log.info("Checking user {} with role {}", user.getUserId(), user.getRole().getName());
            if (UserRole.STUDENT.name().equalsIgnoreCase(user.getRole().getName())) {
                log.info("Skipping user {}: STUDENT role cannot be changed.", user.getUserId());
                skippedCount++;
                continue;
            }
            user.setRole(newRole);
            updatedUsers.add(user);
        }
        if (!updatedUsers.isEmpty()) {
            userAdapter.saveAllUsers(updatedUsers);
            updatedCount = updatedUsers.size();
        }
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new UserEvent(orgId, schema));
                log.info("UserCacheReloadEvent published after bulk role update");
            } catch (Exception e) {
                log.error("Failed to publish UserCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache update bulk role reload.");
        }
        BulkRoleUpdateModel result = new BulkRoleUpdateModel();
        result.setUpdateCount(updatedCount);
        result.setSkippedCount(skippedCount);
        return result;
    }

//    @Override
//    @Transactional
//    public List<BulkWorkScheduleUpdateResponseDto> updateWorkSchedules(
//            BulkWorkScheduleUpdateRequestDto requestDto,
//            String userNameFromToken,
//            String orgId) {
//        String schema = TenantUtil.getCurrentTenant();
//
//        List<BulkWorkScheduleUpdateResponseDto> results = new ArrayList<>();
//
//        WorkScheduleEntity workSchedule = null;
//        if (requestDto.getWorkScheduleId() != null) {
//            workSchedule = workScheduleAdapter.findByScheduleId(requestDto.getWorkScheduleId(), orgId);
//            if (workSchedule == null) {
//                throw new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "WorkSchedule not found with ID: " + requestDto.getWorkScheduleId()
//                );
//            }
//        }
//
//        log.info("Starting bulk work schedule update for scheduleId {}",
//                workSchedule != null ? workSchedule.getScheduleId() : "null");
//
//        Map<String, UserEntity> userMap = userAdapter.getUsersByIds(requestDto.getMemberIds(), orgId)
//                .stream()
//                .collect(Collectors.toMap(UserEntity::getUserId, Function.identity()));
//
//        List<UserEntity> usersToUpdate = new ArrayList<>();
//
//        for (String memberId : requestDto.getMemberIds()) {
//            UserEntity user = userMap.get(memberId);
//
//            if (user == null) {
//                results.add(new BulkWorkScheduleUpdateResponseDto(memberId, false, "User not found"));
//                continue;
//            }
//
//            if (!orgId.equals(user.getOrganizationId())) {
//                results.add(new BulkWorkScheduleUpdateResponseDto(memberId, false, "Unauthorized"));
//                continue;
//            }
//
//            user.setWorkSchedule(workSchedule);
//            usersToUpdate.add(user);
//            results.add(new BulkWorkScheduleUpdateResponseDto(memberId, true, "Work schedule updated"));
//        }
//
//        if (!usersToUpdate.isEmpty()) {
//            try {
//                userAdapter.saveAllUsers(usersToUpdate);
//            } catch (Exception e) {
//                log.error("Failed to save users in batch: {}", e.getMessage());
//            }
//        }
//
//        long successCount = results.stream().filter(BulkWorkScheduleUpdateResponseDto::isSuccess).count();
//        long failedCount = results.size() - successCount;
//        log.info("Completed bulk work schedule update. Success count: {}, Failed count: {}", successCount, failedCount);
//        if (isRedisEnabled) {
//            CacheEventPublisherUtil.syncReloadThenPublish(
//                    publisher,
//                    cacheKeyConfig.getGroups(),
//                    orgId,
//                    schema,
//                    cacheReloadHandlerRegistry
//            );
//            log.info("GroupCacheReloadEvent published after Group Creation");
//        } else {
//            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache create group reload.");
//        }
//        return results;
//    }

    @Override
    @Transactional
    public List<BulkWorkScheduleUpdateResponseDto> updateWorkSchedules(
            BulkWorkScheduleUpdateRequestDto requestDto,
            String userNameFromToken,
            String orgId) {

        String schema = TenantUtil.getCurrentTenant();
        List<BulkWorkScheduleUpdateResponseDto> results = new ArrayList<>();

        WorkScheduleEntity workSchedule = null;
        if (requestDto.getWorkScheduleId() != null) {
            workSchedule = workScheduleAdapter.findByScheduleId(requestDto.getWorkScheduleId(), orgId);
            if (workSchedule == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "WorkSchedule not found with ID: " + requestDto.getWorkScheduleId()
                );
            }
        }

        log.info("Starting bulk work schedule update for scheduleId {}",
                workSchedule != null ? workSchedule.getScheduleId() : "null");

        List<UserEntity> existingUsers = userAdapter.getUsersByIds(requestDto.getMemberIds(), orgId);

        Set<String> existingUserIds = existingUsers.stream()
                .map(UserEntity::getUserId)
                .collect(Collectors.toSet());

        for (String memberId : requestDto.getMemberIds()) {
            if (!existingUserIds.contains(memberId)) {
                results.add(new BulkWorkScheduleUpdateResponseDto(memberId, false, "User not found"));
            } else {
                results.add(new BulkWorkScheduleUpdateResponseDto(memberId, true, "Work schedule updated"));
            }
        }
        if (!existingUserIds.isEmpty()) {
            userAdapter.bulkUpdateWorkSchedule(workSchedule, new ArrayList<>(existingUserIds), orgId);
        }
        long successCount = results.stream().filter(BulkWorkScheduleUpdateResponseDto::isSuccess).count();
        long failedCount = results.size() - successCount;
        log.info("Completed bulk work schedule update. Success count: {}, Failed count: {}", successCount, failedCount);
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new WorkScheduleEvent(orgId, schema));
                log.info("WorkScheduleCacheReloadEvent published after Active update");
            } catch (Exception e) {
                log.error("Failed to publish WorkScheduleCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled. Skipping cache reload.");
        }
        return results;
    }


    @Override
    @Transactional
    public ApiResponse addOrUpdateGroupMembers(String orgId, UserGroupModel model) {
        String schema = TenantUtil.getCurrentTenant();

        if (model == null || model.getGroupIds() == null || model.getUserIds() == null ||
                model.getGroupIds().isEmpty() || model.getUserIds().isEmpty()) {
            log.warn("No user-groups provided for orgId={}", orgId);
            return new ApiResponse(404, "No users or groups provided", null);
        }

        List<Long> groupIds = model.getGroupIds();
        List<String> userIds = model.getUserIds();
        String type = normalizeType(model.getType());

        log.info("Processing {} users × {} groups = {} total mappings for orgId={}",
                userIds.size(), groupIds.size(), userIds.size() * groupIds.size(), orgId);

        List<GroupEntity> groupEntities = userAdapter.findGroupsByIds(new HashSet<>(groupIds));
        List<UserEntity> userEntities = userAdapter.getUsersByIds(userIds, orgId);
        List<UserGroupEntity> existingUserGroups = userAdapter.findUserGroupsByUsersAndGroups(
                new HashSet<>(userIds), new HashSet<>(groupIds));

        Map<Long, GroupEntity> groupMap = new HashMap<>(groupEntities.size());
        for (GroupEntity g : groupEntities) {
            groupMap.put(g.getGroupId(), g);
        }

        Map<String, UserEntity> userMap = new HashMap<>(userEntities.size());
        for (UserEntity u : userEntities) {
            if (u.isActive()) {
                userMap.put(u.getUserId(), u);
            }
        }

        if (groupMap.size() != groupIds.size()) {
            Set<Long> missingGroups = new HashSet<>(groupIds);
            missingGroups.removeAll(groupMap.keySet());
            log.error("Groups not found: {}", missingGroups);
            return new ApiResponse(404, "Group(s) not found: " + missingGroups, null);
        }

        if (userMap.size() != userIds.size()) {
            Set<String> missingUsers = new HashSet<>(userIds);
            missingUsers.removeAll(userMap.keySet());
            log.error("Users not found or inactive: {}", missingUsers);
            return new ApiResponse(404, "User(s) not found or inactive: " + missingUsers, null);
        }

        Map<String, Map<Long, UserGroupEntity>> existingMap = new HashMap<>();
        for (UserGroupEntity e : existingUserGroups) {
            existingMap
                    .computeIfAbsent(e.getUser().getUserId(), k -> new HashMap<>())
                    .put(e.getGroup().getGroupId(), e);
        }
        List<UserGroupEntity> toSave = new ArrayList<>();
        int updates = 0;

        for (String userId : userIds) {
            UserEntity user = userMap.get(userId);
            Map<Long, UserGroupEntity> userGroupMap =
                    existingMap.getOrDefault(userId, Collections.emptyMap());

            for (Long groupId : groupIds) {
                GroupEntity group = groupMap.get(groupId);
                UserGroupEntity existing = userGroupMap.get(groupId);

                if (existing != null) {
                    if (!normalizeType(existing.getType()).equals(type)) {
                        existing.setType(type);
                        toSave.add(existing);
                        updates++;
                    }
                } else {
                    UserGroupEntity entity = new UserGroupEntity();
                    entity.setUser(user);
                    entity.setGroup(group);
                    entity.setType(type);
                    toSave.add(entity);
                }
            }
        }
        boolean cacheReloadRequired = false;
        if (!toSave.isEmpty()) {
            try {
                userAdapter.saveAllUserGroups(toSave);
                log.info("Saved {} user-group mappings ({} updates)", toSave.size(), updates);
                cacheReloadRequired = true;
            } catch (Exception e) {
                log.error("Failed to save user-group entities", e);
                return new ApiResponse(500, "Error saving user-group entities", null);
            }
        } else {
            log.info("No changes to save for orgId={}", orgId);
        }

        if (isRedisEnabled && cacheReloadRequired) {
            try {
                publisher.publishEvent(new GroupEvent(orgId, authHelper.getSchema()));
                log.info("GroupCacheReloadEvent published after Group Update for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish GroupCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload of group members for orgId={}", orgId);
        }
        return new ApiResponse(200, "Users added/updated successfully", null);
    }

    private String normalizeType(String type) {
        if (type == null) return MemberType.MEMBER.getValue();
        return type.trim().equalsIgnoreCase(MemberType.SUPERVISOR.getValue()) ?
                MemberType.SUPERVISOR.getValue() :
                MemberType.MEMBER.getValue();
    }

    @Override
    public Long getSubscribedUserLimit(String orgId) {
        return userAdapter.getSubscribedUserLimit(orgId);
    }

    @Override
    public Long getCurrentUserCount(String orgId) {
        return userAdapter.getCurrentUserCount(orgId);

    }

    @Override
    @Transactional
    public BulkUserLocationModel assignLocations(BulkUserLocationModel model, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        if (model.getMemberIds() == null || model.getMemberIds().isEmpty()
                || model.getLocationIds() == null || model.getLocationIds().isEmpty()) {
            throw new IllegalArgumentException("Member IDs or Location IDs cannot be empty");
        }
        List<String> errors = new ArrayList<>();
        UserLocationEntity saveLocation = new UserLocationEntity();
        BulkUserLocationModel toModel = userEntityMapper.toModel(saveLocation);
        for (String userId : model.getMemberIds()) {
            Optional<UserEntity> userOpt = userAdapter.findById(userId);
            if (userOpt.isEmpty()) {
                errors.add("User ID not found: " + userId);
                continue;
            }
            for (Long locationId : model.getLocationIds()) {
                Optional<LocationEntity> locationOpt = userAdapter.findLocationById(locationId);
                if (locationOpt.isEmpty()) {
                    errors.add("Location ID not found: " + locationId);
                    continue;
                }
                boolean exists = userAdapter.exists(userId, locationId);
                if (!exists) {
                    UserLocationEntity mapping = new UserLocationEntity();
                    mapping.setUser(userOpt.get());
                    mapping.setLocation(locationOpt.get());
                    userAdapter.save(mapping);
                }
            }
        }
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new LocationEvent(orgId, schema));
                log.info("LocationCacheReloadEvent published after the for bulk users location update for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish locationCacheReloadEvent for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload for bulk location update for orgId={}", orgId);
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(", ", errors));
        }
        return toModel;
    }

    @Transactional
    @Override
    public boolean UpdateCalendar(UserCalendarRequestDto updates) {
        String orgId = authHelper.getOrgId();
        String schema = authHelper.getSchema();
        List<UserEntity> users = userAdapter.getUsersByIds(updates.getUserIds());
        if (users.isEmpty()) {
            log.warn("No users found for given IDs: {}", updates.getUserIds());
            return false;
        }
        String newCalendarId = updates.getCalendarId();
        if (newCalendarId == null || newCalendarId.isBlank()) {
            log.warn("Invalid empty calendarId");
            throw new IllegalArgumentException("calendarId cannot be null or empty");
        }
        for (UserEntity user : users) {
            updateUserCalendar(user, newCalendarId);
        }
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new UserEvent(orgId, schema));
                log.info("User cache reload event published after assigned calendar to a user for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish User cache reload event for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload for orgId={}", orgId);
        }
        return true;
    }

    @Override
    public List<UserLevelModel> getUsersBelowHierarchy(String userId, String orgId) {
        UserEntity loggedInUser = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        int userHierarchyLevel = loggedInUser.getRole().getHierarchyLevel();
        List<UserEntity> filteredUsers = userAdapter.getAllUsers(orgId, userId, userHierarchyLevel);
        return userEntityMapper.toUserModelList(filteredUsers);
    }

    @Override
    public List<GroupModel> getSupervisorGroups(String userId) {
        List<GroupEntity> entities = userAdapter.getSupervisorGroups(userId);
        if (entities.isEmpty()) throw new IllegalArgumentException("No group found");
        return userEntityMapper.toModelList(entities);
    }

    @Override
    public List<UserLevelModel> getGroupMembers(Long groupId) {
        List<UserEntity> entities = userAdapter.getGroupMembers(groupId);
        if (entities.isEmpty()) throw new IllegalArgumentException("No group found");
        return userEntityMapper.toUserModelList(entities);
    }

    @Override
    public List<UserLevelModel> getRequesters() {
        String approverId = authHelper.getUserId();
        String role = authHelper.getRole();
        List<UserEntity> users;
        if (RoleName.SUPERADMIN.getRoleName().equalsIgnoreCase(role)) {
            users = userAdapter.getallUsers(approverId);
        } else {
            users = userAdapter.findByApproverId(approverId);
        }
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users found");
        }
        return userEntityMapper.toUserModelList(users);
    }

    @Override
    public RequestApproverModel assignRequestApprover(RequestApproverDto dto) {
        String orgId = authHelper.getOrgId();
        String schema = authHelper.getSchema();
        String approverId = dto.getRequestId();
        List<String> requestedUserIds = dto.getUserId();
        validateApprover(approverId, requestedUserIds);
        List<UserEntity> activeUsers = userAdapter.findAllById(requestedUserIds);
        Set<String> foundIds = activeUsers.stream()
                .map(UserEntity::getUserId)
                .collect(Collectors.toSet());
        List<String> invalidIds = requestedUserIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!invalidIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "These userIds are invalid or inactive: " + String.join(", ", invalidIds)
            );
        }
        userAdapter.updateApproverForUsers(approverId, requestedUserIds);
        if (isRedisEnabled) {
            try {
                publisher.publishEvent(new UserEvent(orgId, schema));
                log.info("User cache reload event published after assigned request approver to a user for orgId={}", orgId);
            } catch (Exception e) {
                log.error("Failed to publish User cache reload event for orgId={}", orgId, e);
            }
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload for request approver of orgId={}", orgId);
        }
        return userEntityMapper.toModel(approverId, requestedUserIds);
    }

    private void validateApprover(String approverId, List<String> userIds) {
        userAdapter.findById(approverId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Approver not found or inactive"));
        if (userIds.contains(approverId)) {
            throw new IllegalArgumentException("A user cannot assign himself as approver");
        }
    }

    @Override
    public void updateUserCalendar(UserEntity existingUser, String newCalendarId) {
        String existingCalendarId =
                existingUser.getCalendar() != null ? existingUser.getCalendar().getId() : null;
        if (Objects.equals(newCalendarId,existingCalendarId)) {
            return;
        }
        if (newCalendarId == null) {
            CalendarEntity calendar = calendarAdapter.findDefaultCalendar();
            existingUser.setCalendar(calendar);
        } else {
            CalendarEntity calendar = calendarAdapter.getById(newCalendarId);
            if (calendar == null) {
                throw new RuntimeException("Invalid calendarId: " + newCalendarId);
            }
            existingUser.setCalendar(calendar);
            log.info("Updated calendar for user {} : {} → {}", existingUser.getUserId(),existingUser.getCalendar().getId(), newCalendarId);
        }
    }

}
