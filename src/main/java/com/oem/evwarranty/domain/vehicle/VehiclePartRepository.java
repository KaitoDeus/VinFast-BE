package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.domain.inventory.Part;


import com.oem.evwarranty.domain.vehicle.VehiclePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository for VehiclePart entity operations.
 */
@Repository
public interface VehiclePartRepository extends JpaRepository<VehiclePart, Long> {

    Optional<VehiclePart> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    @Query("SELECT vp FROM VehiclePart vp JOIN FETCH vp.part WHERE vp.vehicle.id = :vehicleId")
    List<VehiclePart> findByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT vp FROM VehiclePart vp WHERE vp.part.id = :partId")
    List<VehiclePart> findByPartId(@Param("partId") Long partId);

    @Query("SELECT vp FROM VehiclePart vp WHERE vp.vehicle.id = :vehicleId AND vp.status = :status")
    List<VehiclePart> findByVehicleIdAndStatus(@Param("vehicleId") Long vehicleId,
            @Param("status") VehiclePart.PartStatus status);

    @Query("SELECT vp FROM VehiclePart vp WHERE vp.warrantyEndDate >= CURRENT_DATE")
    List<VehiclePart> findPartsUnderWarranty();
}


