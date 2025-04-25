package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    @Mapping(target = "orgId", source = "organizationEntity.organizationId")
    Role toMiddleware(RoleEntity entity);

    @Mapping(target = "orgId", source = "organizationEntity.organizationId")
    Location toMiddleware(LocationEntity entity);

    @Mapping(target = "roleId", source = "role.roleId")
    User toMiddleware(UserEntity entity);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "defaultPassword", ignore = true)
    @Mapping(target = "role", expression = "java(user.getRoleId() != null ? new RoleEntity(user.getRoleId()) : null)")

    UserEntity toEntity(User user);

    @Mapping(target = "workSchedule", ignore = true)
    @Mapping(target = "locationEntity", expression = "java(new LocationEntity(group.getLocationId()))")
    @Mapping(target = "groupId", ignore = true)
    @Mapping(target = "organizationEntity", ignore = true)
    GroupEntity toEntity(AddGroup group);

    @Mapping(target = "locationId", source = "locationEntity.locationId")
    @Mapping(target = "workScheduleId", source = "workSchedule.scheduleId")
    Group toMiddleware(GroupEntity entity);

    @Mapping(target = "scheduleId", source = "scheduleId")
    @Mapping(target = "scheduleName", source = "scheduleName")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    @Mapping(target = "restDay", source = "restDay")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "organizationId", source = "organizationEntity.organizationId")
    WorkSchedule toMiddleware(WorkScheduleEntity workScheduleEntity);

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
}
