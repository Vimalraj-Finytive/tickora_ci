package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserGroupRepository extends JpaRepository<UserGroupEntity,Long> {
    List<UserGroupEntity> findByUserUserIdAndGroupGroupId(Long userId, Long groupId);

    @Modifying
    @Transactional
    @Query(value = """
    DELETE FROM user_group
    WHERE group_id = :groupId AND type = 'Supervisor'
""", nativeQuery = true)
    void deleteSupervisorsByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserGroupEntity ug WHERE ug.group.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user_group SET user_id = :newUserId WHERE group_id = :groupId AND type = 'supervisor' ", nativeQuery = true)
    void updateSupervisorUser(@Param("groupId") Long groupId, @Param("newUserId") Long newUserId);

    List<UserGroupEntity> findByGroup_GroupIdAndGroup_OrganizationEntity_OrganizationId(Long groupId, Long orgId);
}