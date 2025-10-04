package com.uniq.tms.tms_microservice.modules.userManagement.mapper;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.BulkRoleUpdate;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.model.UserBulkChangingModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    //    @Mapping(target = "userId", source = "userIds")
//    @Mapping(target = "role.roleId", source = "roleId")
    UserBulkChangingModel todtoModel(BulkRoleUpdate dto);


    //    @Mapping(target = "userIds", source = "userId")
//    @Mapping(target = "roleId", source = "role.roleId")
    BulkRoleUpdate toDto(UserBulkChangingModel model);



    UserBulkChangingModel toModel(UserEntity entity);
    UserEntity toUserEntity(UserBulkChangingModel model);

}
