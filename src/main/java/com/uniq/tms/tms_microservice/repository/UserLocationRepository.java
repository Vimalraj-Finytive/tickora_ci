package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocationEntity, Long> {

    List<UserLocationEntity> findByUser_UserId(Long userId);

    void deleteByUser_UserIdAndLocation_LocationIdIn(Long userId, Set<Long> toDelete);

    List<UserLocationEntity> findByLocation_LocationIdIn(List<Long> defaultLocationId);
}
