package com.oem.evwarranty.domain.inventory;

import com.oem.evwarranty.domain.vehicle.Vehicle;

import com.oem.evwarranty.domain.vehicle.VehiclePart;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Part entity representing parts in the OEM catalog.
 * Each part has a unique part number and can be covered by warranty.
 */
@Entity
@Table(name = "parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_number", unique = true, nullable = false, length = 50)
    private String partNumber;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PartCategory category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String modelCompatibility;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 5;

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL)
    @Builder.Default
    private List<VehiclePart> vehicleParts = new ArrayList<>();

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Inventory> inventoryItems = new ArrayList<>();

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

    public enum PartCategory {
        BATTERY,
        MOTOR,
        CONTROLLER,
        CHARGER,
        SUSPENSION,
        BRAKES,
        ELECTRONICS,
        BODY,
        INTERIOR,
        HVAC,
        OTHER
    }
}



