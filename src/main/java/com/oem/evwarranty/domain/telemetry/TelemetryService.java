package com.oem.evwarranty.domain.telemetry;

import com.oem.evwarranty.common.config.CacheConfig;
import com.oem.evwarranty.common.enums.EngineStatus;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TelemetryService {

    private final TelemetryLogRepository telemetryLogRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final VehicleRepository vehicleRepository;
    private final TelemetryMapper telemetryMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public TelemetryService(TelemetryLogRepository telemetryLogRepository,
                            ChargingStationRepository chargingStationRepository,
                            VehicleRepository vehicleRepository,
                            TelemetryMapper telemetryMapper,
                            SimpMessagingTemplate messagingTemplate) {
        this.telemetryLogRepository = telemetryLogRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.vehicleRepository = vehicleRepository;
        this.telemetryMapper = telemetryMapper;
        this.messagingTemplate = messagingTemplate;
    }

    public List<TelemetryDTO.LiveVehicleTrackingDTO> getLiveFleetTelemetry() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<TelemetryLog> latestLogs = telemetryLogRepository.findLatestTelemetryForAllVehicles();
        Map<Long, TelemetryLog> logMap = latestLogs.stream()
                .filter(l -> l.getVehicle() != null)
                .collect(Collectors.toMap(l -> l.getVehicle().getId(), l -> l, (a, b) -> a));

        return vehicles.stream()
                .map(v -> {
                    TelemetryLog log = logMap.get(v.getId());
                    return telemetryMapper.toLiveDTO(v, log);
                })
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.CACHE_CHARGING_STATIONS)
    public List<TelemetryDTO.ChargingStationDTO> getChargingStations() {
        return chargingStationRepository.findAll().stream()
                .map(telemetryMapper::toStationDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = CacheConfig.CACHE_CHARGING_STATIONS, allEntries = true)
    public ChargingStation addChargingStation(ChargingStation station) {
        return chargingStationRepository.save(station);
    }

    public TelemetryDTO.LiveVehicleTrackingDTO ingestTelemetry(TelemetryDTO.TelemetryIngestRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + request.getVehicleId()));

        EngineStatus engineStatus = EngineStatus.ON;
        if (request.getEngineStatus() != null) {
            try {
                engineStatus = EngineStatus.valueOf(request.getEngineStatus().toUpperCase());
            } catch (Exception ignored) {}
        }

        TelemetryLog log = TelemetryLog.builder()
                .vehicle(vehicle)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speedKmh(request.getSpeedKmh())
                .heading(request.getHeading())
                .batteryPercent(request.getBatteryPercent())
                .motorTemperature(request.getMotorTemperature())
                .engineStatus(engineStatus)
                .statusText(request.getStatusText())
                .recordedAt(LocalDateTime.now())
                .build();

        TelemetryLog savedLog = telemetryLogRepository.save(log);

        // Update vehicle state
        if (request.getBatteryPercent() != null) {
            vehicle.setBatteryFuelPercent(request.getBatteryPercent());
            vehicleRepository.save(vehicle);
        }

        TelemetryDTO.LiveVehicleTrackingDTO liveDTO = telemetryMapper.toLiveDTO(vehicle, savedLog);

        // Broadcast to WebSocket subscribers
        try {
            messagingTemplate.convertAndSend("/topic/telemetry/fleet", liveDTO);
        } catch (Exception ignored) {}

        return liveDTO;
    }
}
