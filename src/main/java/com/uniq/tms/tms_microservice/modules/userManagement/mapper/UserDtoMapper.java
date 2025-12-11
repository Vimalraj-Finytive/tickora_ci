package com.uniq.tms.tms_microservice.modules.userManagement.mapper;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.userManagement.model.*;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserProjection;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleTypeEntity;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserDtoMapper.CommonMapper.class})
public interface UserDtoMapper {

    @Mapping(target = "roleId" , source = "roleId")
    @Mapping(target = "workSchedule", source = "workSchedule")
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toMiddleware(UserDto dto);

    @Mapping(target = "groupId", source = "groupId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "type" , source = "type")
    UserGroup toMiddleware(EditUserGroupDto editUserGroupDto);

    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "locationId", target = "locationId")
    @Mapping(source = "supervisorsId", target = "supervisorsId")
    @Mapping(source = "type", target = "type")
    AddGroup toMiddleware(AddGroupDto dto);
    AddMember toMiddleware(AddMemberDto addMemberDto);
    UserDto toDto(User user);
    GroupDto toGroupDto(Group group);
    UserResponseDto toDto(UserResponse userResponse);

    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "group.groupName", target = "groupName")
    @Mapping(source = "group.locationEntity", target = "location")
    @Mapping(source = "group.workSchedule.scheduleName", target = "workSchedule")
    @Mapping(source = "type", target = "userType")
    UserGroupProfileDto toGroupsDto(UserGroupEntity entity);

    GroupBulkDeleteModel toModel(GroupBulkDeleteDto dto);

    DeleteMemberModel toModel(DeleteMemberDto dto);
    @Mapping(source = "groupId", target = "groupId")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "type", target = "type")
    UserGroup toModel(Long groupId, String userId, String type);

    UserGroupModel toModel(AddOrUpdateGroupMembersDto dto);

    BulkRoleUpdateDto toDto(BulkRoleUpdateModel model);

    BulkRoleUpdateModel toModel(UserEntity entity);

    BulkUserLocationDto toDto(BulkUserLocationModel model);

    BulkUserLocationModel toModel(BulkUserLocationDto dto);

    @Mapping(target = "policies", ignore = true)
    @Mapping(target = "secondaryDetails", ignore = true)
    UserResponseDto toUserDto(UserProjection userProjection);

    @Mapper(componentModel = "spring")
    interface CommonMapper {
        default String map(WorkScheduleTypeEntity entity) {
            return entity != null ? entity.getTypeId() : null;
        }

        default WorkScheduleTypeEntity map(String id) {
            if (id == null) return null;
            WorkScheduleTypeEntity type = new WorkScheduleTypeEntity();
            type.setTypeId(id);
            return type;
        }
    }

    EditUser toMiddleware(EditUserDto dto);

    List<EditUserDto> toDto(List<UserEntity> user);

    default List<String> map (String value){
        return value != null ? List.of(value) : null;
    }

    BulkRoleUpdateModel toModel(BulkRoleUpdateDto dto);

    UserLevelDto toDto(UserLevelModel model);

    UserLevelModel toModel(UserLevelDto dto);

    List<UserLevelDto> toDtoList(List<UserLevelModel> models);

    List<UserLevelModel> toModelList(List<UserLevelDto> dtos);

    GroupDto toDto(GroupModel model);

    GroupModel toModel(GroupDto dto);

    List<GroupDto> todtoList(List<GroupModel> model);

    RequestApproverDto toDto(RequestApproverModel model);

    RequestApproverModel toModel(RequestApproverDto dto);

    @AfterMapping
    default void addPolicies(UserProjection user, @MappingTarget UserResponseDto dto) {

        dto.setPolicies(new ArrayList<>());
        if (user.getSecName() != null) {
            dto.setSecondaryDetails(new SecondaryDetailsDto(
                    user.getSecName(),
                    user.getSecMobile(),
                    user.getSecEmail(),
                    user.getSecRelation()
            ));
        }
        if (user.getPolicyName() != null) {
            dto.getPolicies().add(
                    new UserPolicyDto(
                            user.getPolicyId(),
                            user.getPolicyName(),
                            user.getValidFrom(),
                            user.getValidTo()
                    )
            );
        }
    }
}
