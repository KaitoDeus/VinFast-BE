package com.oem.evwarranty.domain.campaign;

import com.oem.evwarranty.domain.vehicle.Vehicle;

import com.oem.evwarranty.domain.user.User;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ServiceCampaign entity representing recall campaigns for vehicle issues.
 */
@Entity
@Table(name = "service_campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_number", unique = true, nullable = false, length = 20)
    private String campaignNumber;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 20)
    private CampaignType campaignType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", length = 20)
    @Builder.Default
    private SeverityLevel severityLevel = SeverityLevel.LOW;

    @Column(name = "affected_models", length = 500)
    private String affectedModels;

    @Column(name = "affected_vins", columnDefinition = "TEXT")
    private String affectedVins;

    @Column(name = "affected_parts", length = 500)
    private String affectedParts;

    @Column(name = "remedy_description", columnDefinition = "TEXT")
    private String remedyDescription;

    @Column(name = "estimated_repair_time")
    private Double estimatedRepairTime;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_affected")
    private Integer totalAffected;

    @Column(name = "completed_count")
    @Builder.Default
    private Integer completedCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (campaignNumber == null) {
            campaignNumber = "SC" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CampaignType {
        RECALL,
        SERVICE_BULLETIN,
        SAFETY_RECALL,
        CUSTOMER_SATISFACTION
    }

    public enum CampaignStatus {
        DRAFT,
        ACTIVE,
        PAUSED,
        COMPLETED,
        CANCELLED
    }

    public enum SeverityLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}



