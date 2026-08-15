package com.oem.evwarranty.domain.preorder;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
