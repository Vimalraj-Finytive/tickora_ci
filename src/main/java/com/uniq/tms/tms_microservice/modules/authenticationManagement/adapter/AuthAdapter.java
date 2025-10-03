package com.uniq.tms.tms_microservice.modules.authenticationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.userManagement.entity.SecondaryDetailsEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import java.util.Optional;

public interface AuthAdapter {
    UserEntity findByEmail(String email);
    UserEntity findUserByEmail(String email);
    UserEntity findStudentIdByMobile(String mobile);
    UserEntity findByMobileNumber(String mobile);
    Optional<SecondaryDetailsEntity> findParentByMobile(String loginInput);
}
