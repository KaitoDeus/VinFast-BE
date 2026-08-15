package com.oem.evwarranty.domain.calendar;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class CalendarEventDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventResponse {
        private String id; // "EVT-001" or "BK-2028-001"
        private String title; // "Rental - VinFast VF 8"
        private String type; // "RENTAL", "MAINTENANCE", "INSPECTION", "DELIVERY"
        private String status; // "CONFIRMED", "PENDING", "COMPLETED", "CANCELED"
        private LocalDate startDate;
        private LocalDate endDate;
        private String startTime; // "09:00 AM"
        private String endTime; // "06:00 PM"
        private Long vehicleId;
        private String vehicleName; // "VinFast VF 8 Plus"
        private String customerName; // "Alice Johnson"
        private String driverName; // "Nguyen Van Tai"
        private String notes;
        private String color; // "#0055A5", "#10B981", "#F59E0B"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateEventRequest {
        @NotNull(message = "Title is required")
        private String title;

        private String type = "MAINTENANCE";
        private String status = "CONFIRMED";

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        private String startTime = "09:00 AM";
        private String endTime = "05:00 PM";
        private Long vehicleId;
        private String vehicleName;
        private String customerName;
        private String driverName;
        private String notes;
        private String color = "#0055A5";
    }
}
