package com.oem.evwarranty.domain.preorder;

import com.oem.evwarranty.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pre_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "preorder_code", length = 50)
    private String preorderCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 50)
    private String color;

    @Column(name = "scooter_model", nullable = false, length = 100)
    private String scooterModel;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "deposit_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.valueOf(2000000.0);

    @Column(name = "account_created")
    @Builder.Default
    private Boolean accountCreated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (preorderCode == null || preorderCode.isBlank()) {
            preorderCode = "PO-2028-" + String.format("%04d", System.currentTimeMillis() % 10000);
        }
    }
}
