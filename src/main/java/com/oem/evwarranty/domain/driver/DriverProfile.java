package com.oem.evwarranty.domain.driver;

import com.oem.evwarranty.common.enums.DriverDutyStatus;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_vehicle_id")
    private Vehicle assignedVehicle;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private DriverDutyStatus status = DriverDutyStatus.ON_DUTY;

    @Column(name = "experience_years")
    @Builder.Default
    private Integer experienceYears = 3;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.valueOf(4.90);

    @Column(name = "performance_badge", length = 50)
    @Builder.Default
    private String performanceBadge = "Top Rated";

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "monthly_work_hours")
    @Builder.Default
    private Integer monthlyWorkHours = 160;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

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
}
