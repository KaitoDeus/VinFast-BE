package com.oem.evwarranty.domain.telemetry;

import com.oem.evwarranty.domain.vehicle.Vehicle;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TelemetryMapper {

    public TelemetryDTO.LiveVehicleTrackingDTO toLiveDTO(Vehicle vehicle, TelemetryLog log) {
        if (vehicle == null) return null;

        String formattedId = "VF-" + String.format("%03d", vehicle.getId() != null ? vehicle.getId() : 1);
        String name = vehicle.getModelName() != null ? vehicle.getModelName() : (vehicle.getModel() != null ? vehicle.getModel() : "VinFast VF 8");
        String plateNumber = vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "30A-888.88";

        double lat = (log != null && log.getLatitude() != null) ? log.getLatitude() : 10.7769;
        double lng = (log != null && log.getLongitude() != null) ? log.getLongitude() : 106.7009;
        double speed = (log != null && log.getSpeedKmh() != null) ? log.getSpeedKmh() : 42.0;
        int battery = (log != null && log.getBatteryPercent() != null) ? log.getBatteryPercent() : (vehicle.getBatteryFuelPercent() != null ? vehicle.getBatteryFuelPercent() : 88);
        double heading = (log != null && log.getHeading() != null) ? log.getHeading() : 135.0;
        String status = (log != null && log.getStatusText() != null) ? log.getStatusText() : "In Transit";
        String engine = (log != null && log.getEngineStatus() != null) ? log.getEngineStatus().name() : "ON";

        String clientName = vehicle.getCustomer() != null ? vehicle.getCustomer().getFullName() : "Alice Johnson";
        String driverName = "Nguyen Van Tai";

        return TelemetryDTO.LiveVehicleTrackingDTO.builder()
                .id(formattedId)
                .numericId(vehicle.getId())
                .name(name)
                .plateNumber(plateNumber)
                .coordinates(List.of(lat, lng))
                .speed(((int) speed) + " km/h")
                .speedKmh(speed)
                .battery(battery + "%")
                .batteryPercent(battery)
                .status(status)
                .heading(heading)
                .driverName(driverName)
                .clientName(clientName)
                .engine(engine)
                .build();
    }

    public TelemetryDTO.ChargingStationDTO toStationDTO(ChargingStation station) {
        if (station == null) return null;

        int total = station.getTotalPorts() != null ? station.getTotalPorts() : 8;
        int available = station.getAvailablePorts() != null ? station.getAvailablePorts() : 5;
        int power = station.getPowerKw() != null ? station.getPowerKw() : 150;

        return TelemetryDTO.ChargingStationDTO.builder()
                .id(station.getId())
                .name(station.getName())
                .address(station.getAddress())
                .coordinates(List.of(station.getLatitude(), station.getLongitude()))
                .portsAvailable(available + "/" + total + " Available")
                .totalPorts(total)
                .availablePorts(available)
                .power(power + " kW")
                .powerKw(power)
                .status(station.getStatus() != null ? capitalize(station.getStatus()) : "Operational")
                .build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
