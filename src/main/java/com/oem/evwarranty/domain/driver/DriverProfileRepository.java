package com.oem.evwarranty.domain.driver;

import com.oem.evwarranty.common.enums.DriverDutyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByUserId(Long userId);

    List<DriverProfile> findByStatus(DriverDutyStatus status);

    @Query("SELECT d FROM DriverProfile d LEFT JOIN d.user u WHERE " +
            "(:query IS NULL OR :query = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:status IS NULL OR d.status = :status)")
    Page<DriverProfile> searchDrivers(@Param("query") String query,
                                      @Param("status") DriverDutyStatus status,
                                      Pageable pageable);
}
