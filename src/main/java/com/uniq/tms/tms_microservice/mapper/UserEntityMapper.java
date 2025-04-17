package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    Role toMiddleware(RoleEntity entity);

    Location toMiddleware(LocationEntity entity);

    @Mapping(target = "roleId", source = "role.roleId")
    User toMiddleware(UserEntity entity);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "defaultPassword", ignore = true)
    @Mapping(target = "role", expression = "java(user.getRoleId() != null ? new RoleEntity(user.getRoleId()) : null)")

    UserEntity toEntity(User user);

    @Mapping(target = "workScheduleId", ignore = true)
    @Mapping(target = "locationEntity", expression = "java(new LocationEntity(group.getLocationId()))")
    @Mapping(target = "groupId", ignore = true)
    @Mapping(target = "organizationEntity", ignore = true)
    GroupEntity toEntity(AddGroup group);


    @Mapping(target = "locationId", source = "locationEntity.locationId")
    Group toMiddleware(GroupEntity entity);

    WorkSchedule toMiddleware(WorkScheduleEntity workScheduleEntity);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "locationId", source = "locationEntity.locationId")
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


    @Named("mapLocation")
    default LocationEntity mapLocation(Long locationId) {
        if (locationId == null) return null;
        return new LocationEntity(locationId);
    }

}
