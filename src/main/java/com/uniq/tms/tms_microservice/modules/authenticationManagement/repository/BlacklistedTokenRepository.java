package com.uniq.tms.tms_microservice.modules.authenticationManagement.repository;

import com.uniq.tms.tms_microservice.modules.authenticationManagement.entity.BlacklistedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedTokenEntity, Long> {
    boolean existsByToken(String token);
}
