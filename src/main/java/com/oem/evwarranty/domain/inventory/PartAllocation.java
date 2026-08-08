package com.oem.evwarranty.domain.inventory;

import com.oem.evwarranty.domain.claim.WarrantyClaim;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "part_allocations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(name = "service_center", nullable = false, length = 100)
    private String serviceCenter;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AllocationStatus status = AllocationStatus.PENDING;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
    }

    public enum AllocationStatus {
        PENDING,
        SHIPPED,
        RECEIVED,
        CANCELLED
    }
}



