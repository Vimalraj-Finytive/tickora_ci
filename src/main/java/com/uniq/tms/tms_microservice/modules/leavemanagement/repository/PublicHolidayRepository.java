package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.PublicHolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHolidayEntity, String> {

    List<PublicHolidayEntity> findByCountry_Code(String countryCode);

    @Query("SELECT MAX(p.id) FROM PublicHolidayEntity p WHERE p.id LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(String prefix);

}
