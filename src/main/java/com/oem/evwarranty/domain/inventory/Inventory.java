package com.oem.evwarranty.domain.inventory;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Inventory entity for tracking parts stock at service centers.
 */
@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(name = "service_center", nullable = false, length = 100)
    private String serviceCenter;

    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Column(name = "quantity_reserved")
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(name = "quantity_on_order")
    @Builder.Default
    private Integer quantityOnOrder = 0;

    @Column(name = "reorder_point")
    @Builder.Default
    private Integer reorderPoint = 5;

    @Column(name = "last_restock_date")
    private LocalDateTime lastRestockDate;

    @Column(length = 100)
    private String location;

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
     * Get available quantity (on hand minus reserved)
     */
    public int getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }

    /**
     * Check if reorder is needed
     */
    public boolean needsReorder() {
        return getAvailableQuantity() <= reorderPoint;
    }
}


