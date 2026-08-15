package com.oem.evwarranty.domain.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelemetryLogRepository extends JpaRepository<TelemetryLog, Long> {

    List<TelemetryLog> findByVehicleIdOrderByRecordedAtDesc(Long vehicleId);

    @Query("SELECT t FROM TelemetryLog t WHERE t.vehicle.id = :vehicleId ORDER BY t.recordedAt DESC LIMIT 1")
    Optional<TelemetryLog> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query(value = "SELECT DISTINCT ON (vehicle_id) * FROM telemetry_logs ORDER BY vehicle_id, recorded_at DESC", nativeQuery = true)
    List<TelemetryLog> findLatestTelemetryForAllVehicles();
}
