package com.oem.evwarranty.domain.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingResponse {
        private String id; // "BK-2028-001"
        private Long numericId;
        private String bookingDate; // "2028-08-01"
        private String clientName; // "Alice Johnson"
        private String clientAvatar; // "/team/avatar-1.png"
        private String carModel; // "VinFast VF 8"
        private String rentalPlan; // "Daily"
        private String rentalPeriod; // "01 Aug - 05 Aug 2028"
        private String driverName; // "Nguyen Van Tai"
        private String paymentStatus; // "Paid"
        private String status; // "Approved"
        private Double totalAmount; // 480.0
        private String notes;
        private Long vehicleId;
        private Long clientId;
        private Long driverId;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingKpiResponse {
        private long upcomingBookings;
        private long pendingBookings;
        private long canceledBookings;
        private long completedBookings;
        @Builder.Default
        private double weeklyGrowthPercentage = 2.77;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBookingRequest {
        private Long clientId;

        @NotNull(message = "Vehicle ID is required")
        private Long vehicleId;

        private Long driverId;
        private String rentalPlan = "DAILY";

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        private String notes;
        private BigDecimal totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull(message = "Status is required")
        private String status;
    }
}
