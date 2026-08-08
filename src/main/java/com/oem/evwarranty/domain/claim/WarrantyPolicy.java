package com.oem.evwarranty.domain.claim;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * WarrantyPolicy entity defining warranty rules and durations.
 */
@Entity
@Table(name = "warranty_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "mileage_limit")
    private Integer mileageLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_type", length = 30)
    private CoverageType coverageType;

    @Column(name = "applicable_models", length = 500)
    private String applicableModels;

    @Column(name = "applicable_parts", length = 500)
    private String applicableParts;

    @Column(name = "deductible_amount")
    private Double deductibleAmount;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

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

    public enum CoverageType {
        FULL,
        POWERTRAIN,
        BATTERY,
        ELECTRONICS,
        BUMPER_TO_BUMPER,
        EXTENDED
    }
}


