package com.uniq.tms.tms_microservice.modules.userManagement.repository;

import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserLocationEntity;
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

    @Query("SELECT ul.user FROM UserLocationEntity ul WHERE ul.location.locationId IN :filteredUsersIds AND ul.user.id <> :userIdFromToken")

    List<UserEntity> findMembersByLocationIds(
            @Param("filteredUsersIds") List<Long> filteredUserIds,
            @Param("userIdFromToken") String userIdFromToken);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN UserLocationEntity ug ON u.userId = ug.user.userId " +
            "WHERE u.active = true AND ug.location.locationId IN :userIds")
    List<UserEntity> findUserByLocationId(@Param("userIds") List<Long> userIds);

}
