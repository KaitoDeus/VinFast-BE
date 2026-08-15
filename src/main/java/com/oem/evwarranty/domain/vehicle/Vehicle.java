package com.oem.evwarranty.domain.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oem.evwarranty.common.enums.CarType;
import com.oem.evwarranty.domain.claim.ServiceHistory;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.customer.Customer;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Vehicle entity representing electric vehicles in the VinFast Platform.
 * Supports both Fleet Rental operations and OEM Warranty lifecycle management.
 */
@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 17)
    private String vin;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String brand = "VinFast";

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "make", length = 100)
    @Builder.Default
    private String make = "VinFast";

    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", length = 30)
    @Builder.Default
    private CarType carType = CarType.SUV;

    @Column(name = "license_plate", length = 30)
    private String licensePlate;

    @Column(name = "daily_price")
    @Builder.Default
    private BigDecimal dailyPrice = BigDecimal.valueOf(120.0);

    @Column
    private Integer year;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    @Builder.Default
    private String transmission = "Automatic";

    @Column(length = 50)
    @Builder.Default
    private String capacity = "5 seats";

    @Column(name = "range_km")
    private Integer rangeKm;

    @Column(name = "battery_type", length = 50)
    private String batteryType;

    @Column(name = "battery_capacity")
    private Double batteryCapacity;

    @Column(name = "battery_fuel_percent")
    @Builder.Default
    private Integer batteryFuelPercent = 90;

    @Column(name = "top_speed_kmh")
    private Integer topSpeedKmh;

    @Column(name = "acceleration_spec", length = 50)
    private String accelerationSpec;

    @Column(name = "hero_image_url", length = 255)
    private String heroImageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "units_count")
    @Builder.Default
    private Integer unitsCount = 1;

    @Column(name = "motor_type", length = 50)
    private String motorType;

    @Column
    private Integer mileage;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "warranty_start_date")
    private LocalDate warrantyStartDate;

    @Column(name = "warranty_end_date")
    private LocalDate warrantyEndDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "fleet_status", length = 20)
    @Builder.Default
    private com.oem.evwarranty.common.enums.VehicleStatus fleetStatus = com.oem.evwarranty.common.enums.VehicleStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @JsonIgnore
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VehiclePart> installedParts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WarrantyClaim> warrantyClaims = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceHistory> serviceHistory = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (modelName != null && model == null) {
            model = modelName;
        } else if (model != null && modelName == null) {
            modelName = model;
        }
        if (vin == null || vin.isBlank()) {
            vin = "VF" + (System.currentTimeMillis() % 100000000000000L);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (modelName != null && model == null) {
            model = modelName;
        } else if (model != null && modelName == null) {
            modelName = model;
        }
    }

    public boolean isUnderWarranty() {
        if (warrantyEndDate == null)
            return false;
        return LocalDate.now().isBefore(warrantyEndDate) || LocalDate.now().isEqual(warrantyEndDate);
    }

    public enum VehicleStatus {
        ACTIVE, INACTIVE, SOLD, SCRAPPED
    }
}
