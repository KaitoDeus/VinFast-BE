package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.domain.inventory.Inventory;
import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.inventory.Part;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VehiclePart entity representing installed parts on a vehicle.
 * Tracks part installation with serial number and warranty status.
 */
@Entity
@Table(name = "vehicle_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiclePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", unique = true, nullable = false, length = 50)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "warranty_start_date")
    private LocalDate warrantyStartDate;

    @Column(name = "warranty_end_date")
    private LocalDate warrantyEndDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PartStatus status = PartStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installed_by")
    private User installedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

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
     * Check if part is currently under warranty
     */
    public boolean isUnderWarranty() {
        if (warrantyEndDate == null)
            return false;
        return LocalDate.now().isBefore(warrantyEndDate) || LocalDate.now().isEqual(warrantyEndDate);
    }

    public enum PartStatus {
        ACTIVE, REPLACED, DEFECTIVE, RECALLED
    }
}




