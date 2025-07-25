package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.SecondaryDetailsEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecondaryDetailsRepository extends JpaRepository<SecondaryDetailsEntity,Long> {

    Optional<SecondaryDetailsEntity> findByMobile(@Param("mobile") String mobile);

    Optional<SecondaryDetailsEntity> findByEmail(@Param("email") String email);

    @Query(value = "SELECT u.* FROM users u " +
            "JOIN secondary_details sd ON u.user_id = sd.user_id " +
            "WHERE sd.mobile = :mobile", nativeQuery = true)
    UserEntity findUserByMobile(@Param("mobile") String mobile);

    @Query(value = "SELECT u.* FROM users u " +
            "JOIN secondary_details sd ON u.user_id = sd.user_id " +
            "WHERE sd.email = :email", nativeQuery = true)
    UserEntity findUserByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM secondary_details WHERE user_id = :userId", nativeQuery = true)
    Optional<SecondaryDetailsEntity> findByUserId(@Param("userId") String userId);

    @Query(value = "SELECT mobile FROM secondary_details", nativeQuery = true)
    List<String > findAllMobile(@Param("orgId") String orgId);

    @Query(value = "SELECT email FROM secondary_details", nativeQuery = true)
    List<String > findAllEmail(@Param("orgId") String orgId);

}
