package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.vehicle.Vehicle;


import com.oem.evwarranty.domain.claim.WarrantyClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository for WarrantyClaim entity operations.
 */
@Repository
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {

    Optional<WarrantyClaim> findByClaimNumber(String claimNumber);

    @Query("SELECT wc FROM WarrantyClaim wc WHERE wc.vehicle.id = :vehicleId")
    List<WarrantyClaim> findByVehicleId(@Param("vehicleId") Long vehicleId);

    List<WarrantyClaim> findByStatus(WarrantyClaim.ClaimStatus status);

    @Query("SELECT wc FROM WarrantyClaim wc WHERE wc.serviceCenter = :serviceCenter")
    List<WarrantyClaim> findByServiceCenter(@Param("serviceCenter") String serviceCenter);

    @Query("SELECT wc FROM WarrantyClaim wc WHERE wc.submittedBy.id = :userId")
    List<WarrantyClaim> findBySubmittedBy(@Param("userId") Long userId);

    @Query("SELECT wc FROM WarrantyClaim wc WHERE wc.technician.id = :technicianId")
    List<WarrantyClaim> findByTechnician(@Param("technicianId") Long technicianId);

    @Query("SELECT wc FROM WarrantyClaim wc WHERE wc.status IN :statuses")
    Page<WarrantyClaim> findByStatusIn(@Param("statuses") List<WarrantyClaim.ClaimStatus> statuses, Pageable pageable);

    @Query("SELECT wc FROM WarrantyClaim wc WHERE " +
            "LOWER(wc.claimNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(wc.vehicle.vin) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<WarrantyClaim> searchClaims(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(wc) FROM WarrantyClaim wc WHERE wc.status = :status")
    Long countByStatus(@Param("status") WarrantyClaim.ClaimStatus status);

    @Query("SELECT wc FROM WarrantyClaim wc WHERE wc.serviceCenter = :serviceCenter ORDER BY wc.createdAt DESC")
    Page<WarrantyClaim> findByServiceCenterPaged(@Param("serviceCenter") String serviceCenter, Pageable pageable);
}


