package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.vehicle.Vehicle;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ServiceHistory entity for tracking all service records of a vehicle.
 */
@Entity
@Table(name = "service_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "service_center", length = 100)
    private String serviceCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;

    @Column(name = "mileage_at_service")
    private Integer mileageAtService;

    @Column(name = "labor_hours")
    private Double laborHours;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warranty_claim_id")
    private WarrantyClaim warrantyClaim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_campaign_id")
    private ServiceCampaign serviceCampaign;

    @Column(name = "service_date")
    private LocalDateTime serviceDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (serviceDate == null) {
            serviceDate = LocalDateTime.now();
        }
    }

    public enum ServiceType {
        ROUTINE_MAINTENANCE,
        WARRANTY_REPAIR,
        RECALL_SERVICE,
        DIAGNOSTIC,
        BATTERY_SERVICE,
        SOFTWARE_UPDATE,
        BODY_REPAIR,
        OTHER
    }
}




