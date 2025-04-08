package com.uniq.tms.tms_microservice.mapper;



import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.model.AddGroup;
import com.uniq.tms.tms_microservice.model.Group;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.Member;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.model.User;
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
    @Mapping(target = "role", expression = "java(user.getRoleId() != null ? new RoleEntity(user.getRoleId()) : null)")
    UserEntity toEntity(User user);

    @Mapping(target = "managerIds", source = "managerIds")
    @Mapping(target = "supervisorsId" , source = "supervisorsId")
    GroupEntity toEntity(AddGroup group);

    @Mapping(target = "managerIds", source = "managerIds")
    @Mapping(target = "locationId", source = "locationEntity.locationId")
    Group toMiddleware(GroupEntity entity);

    @Mapping(target = "managerIds", source = "managerIds")
    @Mapping(target = "locationId", source = "locationEntity.locationId")
    @Mapping(target = "supervisorsId" , source = "supervisorsId")
    AddGroup toGroupMiddleware(GroupEntity entity);

    @Named("mapLocation")
    default LocationEntity mapLocation(Long locationId) {
        if (locationId == null) return null;
        return new LocationEntity(locationId);
    }

    @Mapping(target = "groupMember", source = "groupMemberIds")
    Member toMemberMiddleware(GroupEntity entity);

}
