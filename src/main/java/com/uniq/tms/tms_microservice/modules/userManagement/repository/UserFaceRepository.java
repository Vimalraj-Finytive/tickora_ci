package com.uniq.tms.tms_microservice.modules.userManagement.repository;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.UserFaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserFaceRepository extends JpaRepository<UserFaceEntity, String> {
    Optional<UserFaceEntity> findByUserId(String userId);

    @Transactional
    void deleteByUserId(String userId);
}
