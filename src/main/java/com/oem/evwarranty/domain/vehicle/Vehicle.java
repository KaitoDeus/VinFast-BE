package com.oem.evwarranty.domain.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oem.evwarranty.domain.claim.ServiceHistory;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.customer.Customer;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Vehicle entity representing electric vehicles in the system.
 * VIN must be unique and follows standard 17-character format.
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

    @Column(unique = true, nullable = false, length = 17)
    private String vin;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, length = 100)
    private String make;

    @Column(nullable = false)
    private Integer year;

    @Column(length = 50)
    private String color;

    @Column(name = "battery_type", length = 50)
    private String batteryType;

    @Column(name = "battery_capacity")
    private Double batteryCapacity;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if vehicle is currently under warranty
     */
    public boolean isUnderWarranty() {
        if (warrantyEndDate == null)
            return false;
        return LocalDate.now().isBefore(warrantyEndDate) || LocalDate.now().isEqual(warrantyEndDate);
    }

    public enum VehicleStatus {
        ACTIVE, INACTIVE, SOLD, SCRAPPED
    }
}
