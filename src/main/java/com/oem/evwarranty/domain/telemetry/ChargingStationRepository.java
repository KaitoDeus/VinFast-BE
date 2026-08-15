package com.oem.evwarranty.domain.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    List<ChargingStation> findByStatus(String status);
}
