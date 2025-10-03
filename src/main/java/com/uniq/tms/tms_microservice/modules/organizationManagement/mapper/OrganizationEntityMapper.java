package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationTypeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.RoleEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Organization;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.OrganizationType;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Privilege;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Role;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserSchemaMappingEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.UserEntityMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationEntityMapper {

    Logger log = LogManager.getLogger(UserEntityMapper.class);
    Role toMiddleware(RoleEntity entity);

    PrivilegeEntity toEntity(Privilege privilegeModel);

    Privilege toModel(PrivilegeEntity privilege);

    OrganizationEntity toEntity(Organization organization);

    Organization toModel(OrganizationEntity organizationEntity);

    OrganizationType toModel(OrganizationTypeEntity organizationType);

    UserSchemaMappingEntity toSchema(String email, String mobile, String orgId, String schemaName);

}
