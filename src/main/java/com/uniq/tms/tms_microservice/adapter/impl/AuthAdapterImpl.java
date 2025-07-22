package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.AuthAdapter;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.repository.SecondaryDetailsRepository;
import com.uniq.tms.tms_microservice.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class AuthAdapterImpl implements AuthAdapter {
    private final UserRepository userRepository;
    private final SecondaryDetailsRepository secondaryDetailsRepository;

    public AuthAdapterImpl(UserRepository userRepository, SecondaryDetailsRepository secondaryDetailsRepository) {
        this.userRepository = userRepository;
        this.secondaryDetailsRepository = secondaryDetailsRepository;
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserEntity findUserByEmail(String email) {
        return secondaryDetailsRepository.findUserByEmail(email);
    }

    @Override
    public UserEntity findStudentIdByMobile(String mobile) {
        return secondaryDetailsRepository.findUserByMobile(mobile);
    }

    @Override
    public UserEntity findByMobileNumber(String mobile) {
        return userRepository.findByMobileNumber(mobile);
    }
}
