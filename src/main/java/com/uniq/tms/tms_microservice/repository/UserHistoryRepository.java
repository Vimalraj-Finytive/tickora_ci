package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistoryEntity, String> {

    List<UserHistoryEntity> findByUserId(String userId);
}
