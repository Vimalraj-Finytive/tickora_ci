package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroupEntity,Long> {

    @Query("SELECT ug FROM UserGroupEntity ug WHERE ug.user.userId = :userId AND ug.group.groupId = :groupId")
    List<UserGroupEntity> findByUserIdAndGroupId(@Param("userId") Long userId,  @Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query("UPDATE UserGroupEntity ug SET ug.type = :type WHERE ug.user.id = :userId AND ug.group.id = :groupId")
    int updateUserGroupType(Long userId, Long groupId, String type);

    @Modifying
    @Transactional
    @Query(value = """
    DELETE FROM user_group
    WHERE group_id = :groupId
      AND user_id IN (:userIds)
""", nativeQuery = true)
    void deleteSupervisorsByGroupId(@Param("groupId") Long groupId, @Param("userIds") Long userIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserGroupEntity ug WHERE ug.group.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user_group SET user_id = :newUserId WHERE group_id = :groupId AND type = 'supervisor' ", nativeQuery = true)
    void updateSupervisorUser(@Param("groupId") Long groupId, @Param("newUserId") Long newUserId);

    @Query("SELECT ug FROM UserGroupEntity ug WHERE ug.group.groupId = :groupId AND ug.group.organizationEntity.organizationId = :orgId AND ug.user.active = true")
    List<UserGroupEntity> findActiveUserGroups(Long groupId, Long orgId);

    @Query("SELECT DISTINCT ug.group.id FROM UserGroupEntity ug WHERE ug.user.userId = :supervisorId AND ug.type = 'Supervisor'")
    List<Long>  findGroupIdsBySupervisorId(Long supervisorId);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN UserGroupEntity ug ON u.userId = ug.user.userId " +
            "WHERE ug.group.groupId IN :groupIds")
    List<UserEntity> findUsersByGroupId(@Param("groupIds") List<Long> groupIds);

    @Query("SELECT ug.user FROM UserGroupEntity ug WHERE ug.group.groupId IN :filteredGroupIds AND ug.type = 'Member' AND ug.user.id != :userIdFromToken")
    List<UserEntity> findMembersByGroupIds(
            @Param("filteredGroupIds") List<Long> filteredGroupIds,
            @Param("userIdFromToken") Long userIdFromToken);

    @Query(value = "SELECT user_id FROM user_group WHERE group_id = :groupId AND type = 'Supervisor'", nativeQuery = true)
    List<Long> findSupervisorIdsByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT u.user.userId FROM UserGroupEntity u WHERE u.group.groupId = :groupId AND u.type = :type")
    List<Long> findUserIdsByGroupIdAndType(@Param("groupId") Long groupId, @Param("type") String type);

    @Query(value = """
    SELECT DISTINCT u.* FROM users u
    JOIN user_group ug ON u.user_id = ug.user_id
    WHERE u.user_id IN (:userIds)
      AND ug.group_id IN (
          SELECT group_id FROM user_group
          WHERE user_id = :supervisorId
            AND type = 'Supervisor'
      )
""", nativeQuery = true)
    List<UserEntity> filterUsersByGroupIds(
            @Param("supervisorId") Long supervisorId,
            @Param("userIds") List<Long> userIds
    );

    @Query("SELECT ug FROM UserGroupEntity ug WHERE ug.group.groupId IN :groupIds AND ug.group.organizationEntity.organizationId = :orgId")
    List<UserGroupEntity> findActiveGroupMembersExcludingSupervisors(
            @Param("groupIds") List<Long> groupIds,
            @Param("orgId") Long orgId);

    List<UserGroupEntity> findGroupByUser_UserId(Long userId);

    void deleteByUser_UserIdAndGroup_GroupIdIn(Long userId, Set<Long> toDelete);
}
