package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserSchemaMappingEntity;
import com.uniq.tms.tms_microservice.shared.exception.CommonExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class TenantResolverHelper {

    private final Logger log = LogManager.getLogger(TenantResolverHelper.class);

    private final UserAdapter userAdapter;

    public TenantResolverHelper(UserAdapter userAdapter){
        this.userAdapter = userAdapter;
    }

    public String getUserSchemaByEmail(String email) {
        UserSchemaMappingEntity entity = userAdapter.findUserByEmail(email);

        if (entity != null) {
            log.info("Found schema for user Email, {}: {}", email, entity.getSchemaName());
            return entity.getSchemaName();
        } else {
            log.warn("No schema found for email: {}", email);
            throw new CommonExceptionHandler.SchemaNotFoundException("Invalid User Email");
        }
    }

    public String getUserSchemaByMobile(String mobile) {
        UserSchemaMappingEntity entity = userAdapter.findUserByMobile(mobile);
        if (entity != null) {
            log.info("Found schema for user mobile: {} -> {}", entity.getMobile(), entity.getSchemaName());
            return entity.getSchemaName();
        } else {
            log.warn("No schema found for mobile number");
            throw new CommonExceptionHandler.SchemaNotFoundException("Invalid User Mobile");
        }
    }
}
