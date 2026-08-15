package com.oem.evwarranty.domain.telemetry;

import com.oem.evwarranty.common.enums.EngineStatus;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "speed_kmh")
    @Builder.Default
    private Double speedKmh = 0.0;

    @Column
    @Builder.Default
    private Double heading = 0.0;

    @Column(name = "battery_percent")
    @Builder.Default
    private Integer batteryPercent = 90;

    @Column(name = "motor_temperature")
    @Builder.Default
    private Double motorTemperature = 38.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine_status", length = 20)
    @Builder.Default
    private EngineStatus engineStatus = EngineStatus.ON;

    @Column(name = "status_text", length = 50)
    @Builder.Default
    private String statusText = "In Transit";

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
