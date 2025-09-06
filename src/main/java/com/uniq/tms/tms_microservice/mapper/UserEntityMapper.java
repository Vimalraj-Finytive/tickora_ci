package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.LocationDto;
import com.uniq.tms.tms_microservice.dto.UserHistoryResponseDto;
import com.uniq.tms.tms_microservice.dto.UserValidationDto;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEntityMapper {

    Logger log = LogManager.getLogger(UserEntityMapper.class);
    Role toMiddleware(RoleEntity entity);

    @Mapping(target = "orgId", source = "organizationEntity.organizationId")
    @Mapping(target = "locationId", source = "locationId")
    Location toMiddleware(LocationEntity entity);

    @Mapping(target = "roleId", source = "role.roleId")
    @Mapping(target = "workSchedule", source = "workSchedule.scheduleName")
    User toMiddleware(UserEntity entity);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "defaultPassword", ignore = true)
    @Mapping(target = "role", expression = "java(user.getRoleId() != null ? new RoleEntity(user.getRoleId()) : null)")

    @Mapping(target = "workSchedule.scheduleName", source = "workSchedule")
    @Mapping(target = "workSchedule.type", ignore = true)
    @Mapping(target = "workSchedule.scheduleId", source = "workSchedule")
    UserEntity toEntity(User user);

    @Mapping(target = "workSchedule", ignore = true)
    @Mapping(target = "locationEntity", expression = "java(new LocationEntity(group.getLocationId()))")
    @Mapping(target = "groupId", ignore = true)
    @Mapping(target = "organizationEntity", ignore = true)
    GroupEntity toEntity(AddGroup group);

    @Mapping(target = "locationId", source = "locationEntity.locationId")
    @Mapping(target = "workScheduleId", source = "workSchedule.scheduleId")
    Group toMiddleware(GroupEntity entity);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "locationId", source = "locationEntity.locationId")
    @Mapping(target = "workScheduleId", source = "workSchedule.scheduleId")
    @Mapping(target = "supervisorsId", ignore = true)
    AddGroup toGroupMiddleware(GroupEntity entity);

    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "type", target = "type")
    UserGroup toMiddleware(UserGroupEntity savedEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(new UserEntity(userGroup.getUserId()))")
    @Mapping(target = "group", expression = "java(new GroupEntity(userGroup.getGroupId()))")
    UserGroupEntity toEntity(UserGroup userGroup);

    @Mapping(target = "workSchedule.scheduleName", source = "workSchedule")
    @Mapping(target = "workSchedule.type", ignore = true)
    UserEntity toMiddleware(User user);

    Location toMiddleware(LocationDto locationDto);

    PrivilegeEntity toEntity(Privilege privilegeModel);

    Privilege toModel(PrivilegeEntity privilege);

    LocationEntity toEntity(Location location);

    OrganizationEntity toEntity(Organization organization);

    Organization toModel(OrganizationEntity organizationEntity);

    OrganizationType toModel(OrganizationTypeEntity organizationType);

    UserSchemaMappingEntity toSchema(String email, String mobile, String orgId, String schemaName);

    UserValidationDto toDto(UserEntity user);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "activeStatus", expression = "java(com.uniq.tms.tms_microservice.enums.UserStatusTypeEnum.INACTIVE.getValue())")
    @Mapping(target = "comments", source = "comments")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    UserHistoryEntity toInactiveUserEntity(String userId, String comments);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "activeStatus", expression = "java(com.uniq.tms.tms_microservice.enums.UserStatusTypeEnum.ACTIVE.getValue())")
    @Mapping(target = "comments", source = "comments")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    UserHistoryEntity toActiveUserEntity(String userId, String comments);

    List<UserHistoryResponseDto> toHistoryDto(List<UserHistoryEntity> responseDtos);
}
