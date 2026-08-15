package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.domain.customer.Customer;


import com.oem.evwarranty.domain.vehicle.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Vehicle entity operations.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByVin(String vin);

    boolean existsByVin(String vin);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:query IS NULL OR :query = '' OR LOWER(v.model) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(v.modelName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(v.vin) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:carType IS NULL OR v.carType = :carType) AND " +
            "(:status IS NULL OR v.fleetStatus = :status)")
    Page<Vehicle> findVehiclesWithFilters(@Param("query") String query,
                                          @Param("carType") com.oem.evwarranty.common.enums.CarType carType,
                                          @Param("status") com.oem.evwarranty.common.enums.VehicleStatus status,
                                          Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.customer.id = :customerId")
    List<Vehicle> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT v FROM Vehicle v WHERE " +
            "LOWER(v.vin) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.make) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Vehicle> searchVehicles(@Param("search") String search, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.model = :model")
    List<Vehicle> findByModel(@Param("model") String model);

    @Query("SELECT v FROM Vehicle v WHERE v.warrantyEndDate >= CURRENT_DATE")
    List<Vehicle> findVehiclesUnderWarranty();

    @Query("SELECT v FROM Vehicle v WHERE v.warrantyEndDate < CURRENT_DATE")
    List<Vehicle> findVehiclesOutOfWarranty();

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status")
    Long countByStatus(@Param("status") Vehicle.VehicleStatus status);
}


