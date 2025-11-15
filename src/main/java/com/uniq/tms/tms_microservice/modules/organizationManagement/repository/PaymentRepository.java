package com.uniq.tms.tms_microservice.modules.organizationManagement.repository;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {

    Optional<PaymentEntity> findByOrderId(String orderId);


    @Query("SELECT TO_CHAR(p.paymentDate, 'FMMonth') AS month, SUM(p.amount) AS totalAmount " +
            "FROM PaymentEntity p " +
            "WHERE EXTRACT(YEAR FROM p.paymentDate) = :year " +
            "GROUP BY TO_CHAR(p.paymentDate, 'FMMonth'), EXTRACT(MONTH FROM p.paymentDate) " +
            "ORDER BY EXTRACT(MONTH FROM p.paymentDate)")
    List<Object[]> getMonthlyAmountWithFullMonthName(@Param("year") int year);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p " +
            "WHERE EXTRACT(YEAR FROM p.paymentDate) = :year")
    BigDecimal getTotalAmountByYear(@Param("year") int year);

}
