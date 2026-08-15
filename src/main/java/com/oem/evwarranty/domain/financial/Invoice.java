package com.oem.evwarranty.domain.financial;

import com.oem.evwarranty.common.enums.InvoiceStatus;
import com.oem.evwarranty.common.enums.PaymentMethod;
import com.oem.evwarranty.domain.booking.Booking;
import com.oem.evwarranty.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private User client;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.COMPLETED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.VNPAY;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            invoiceNumber = "INV-2028-" + String.format("%04d", System.currentTimeMillis() % 10000);
        }
        if (dueDate == null) {
            dueDate = LocalDate.now().plusDays(7);
        }
    }
}
