package com.oem.evwarranty.domain.telemetry;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "charging_stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "total_ports")
    @Builder.Default
    private Integer totalPorts = 8;

    @Column(name = "available_ports")
    @Builder.Default
    private Integer availablePorts = 5;

    @Column(name = "power_kw")
    @Builder.Default
    private Integer powerKw = 150;

    @Column(length = 30)
    @Builder.Default
    private String status = "OPERATIONAL";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
