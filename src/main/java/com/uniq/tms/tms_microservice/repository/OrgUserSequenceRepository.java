package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.OrgUserSequenceEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgUserSequenceRepository extends JpaRepository<OrgUserSequenceEntity, String> {

    @Modifying
    @Transactional
    @Query("UPDATE OrgUserSequenceEntity s SET s.lastNumber = s.lastNumber + 1 WHERE s.orgId = :orgId")
    int incrementSequence(@Param("orgId") String orgId);

    @Query("SELECT s.lastNumber FROM OrgUserSequenceEntity s WHERE s.orgId = :orgId")
    Integer getLastNumber(@Param("orgId") String orgId);
}
