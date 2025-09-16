package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocationEntity, Long> {

    List<UserLocationEntity> findByUser_UserId(String userId);

    void deleteByUser_UserIdAndLocation_LocationIdIn(String userId, Set<Long> toDelete);

    List<UserLocationEntity> findByLocation_LocationIdIn(List<Long> defaultLocationId);

    @Query("SELECT ul FROM UserLocationEntity ul JOIN FETCH ul.location WHERE ul.user.userId = :userId")
    List<UserLocationEntity> fetchLocationsForUser(@Param("userId") String userId);

}
