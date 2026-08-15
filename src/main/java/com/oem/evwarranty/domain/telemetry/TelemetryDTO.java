package com.oem.evwarranty.domain.telemetry;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class TelemetryDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiveVehicleTrackingDTO {
        private String id; // "VF-001"
        private Long numericId;
        private String name; // "VinFast VF 8"
        private String plateNumber; // "30A-888.88"
        private List<Double> coordinates; // [lat, lng]
        private String speed; // "42 km/h"
        private Double speedKmh;
        private String battery; // "88%"
        private Integer batteryPercent;
        private String status; // "In Transit", "Parked"
        private Double heading; // 135.0
        private String driverName; // "Nguyen Van Tai"
        private String clientName; // "Alice Johnson"
        private String engine; // "ON", "OFF"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargingStationDTO {
        private Long id;
        private String name;
        private String address;
        private List<Double> coordinates;
        private String portsAvailable; // "8/12 Available"
        private Integer totalPorts;
        private Integer availablePorts;
        private String power; // "250 kW"
        private Integer powerKw;
        private String status; // "Operational"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TelemetryIngestRequest {
        @NotNull(message = "Vehicle ID is required")
        private Long vehicleId;

        @NotNull(message = "Latitude is required")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        private Double longitude;

        private Double speedKmh = 0.0;
        private Double heading = 0.0;
        private Integer batteryPercent = 90;
        private Double motorTemperature = 38.0;
        private String engineStatus = "ON";
        private String statusText = "In Transit";
    }
}
