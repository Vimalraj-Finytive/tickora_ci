package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserFaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFaceRepository extends JpaRepository<UserFaceEntity, String> {
    Optional<UserFaceEntity> findByUserId(String userId);
}
