package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.vehicle.Vehicle;


import com.oem.evwarranty.domain.claim.ServiceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for ServiceHistory entity operations.
 */
@Repository
public interface ServiceHistoryRepository extends JpaRepository<ServiceHistory, Long> {

    @Query("SELECT sh FROM ServiceHistory sh WHERE sh.vehicle.id = :vehicleId ORDER BY sh.serviceDate DESC")
    List<ServiceHistory> findByVehicleId(@Param("vehicleId") Long vehicleId);

    List<ServiceHistory> findByServiceType(ServiceHistory.ServiceType serviceType);

    @Query("SELECT sh FROM ServiceHistory sh WHERE sh.serviceCenter = :serviceCenter ORDER BY sh.serviceDate DESC")
    Page<ServiceHistory> findByServiceCenter(@Param("serviceCenter") String serviceCenter, Pageable pageable);

    @Query("SELECT sh FROM ServiceHistory sh WHERE sh.technician.id = :technicianId ORDER BY sh.serviceDate DESC")
    List<ServiceHistory> findByTechnicianId(@Param("technicianId") Long technicianId);
}


