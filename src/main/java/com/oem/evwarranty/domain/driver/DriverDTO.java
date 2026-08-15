package com.oem.evwarranty.domain.driver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class DriverDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverResponse {
        private String id; // "DRV-001"
        private Long numericId;
        private String fullName; // "Nguyen Van Tai"
        private String phone; // "+84 912 345 678"
        private String assignedCar; // "VinFast VF 8 - 30A-888.88"
        private Long assignedVehicleId;
        private String dutyStatus; // "On Duty", "In Transit"
        private String status; // "ON_DUTY"
        private Double rating; // 4.95
        private String experience; // "5 years"
        private Integer experienceYears;
        private String performanceBadge; // "Top Rated"
        private String monthlyWorkHours; // "168 hrs"
        private Integer workHours;
        private String avatarUrl; // "/team/driver-1.png"
        private String licenseNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDriverRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Phone number is required")
        private String phone;

        private String email;
        private Long assignedVehicleId;
        private String licenseNumber;
        private Integer experienceYears = 3;
        private String avatarUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDutyStatusRequest {
        @NotNull(message = "Duty status is required")
        private String dutyStatus;
    }
}
