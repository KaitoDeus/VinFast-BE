package com.oem.evwarranty.domain.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class ClientDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientResponse {
        private String id; // "CL-001"
        private Long numericId;
        private String fullName; // "Alice Johnson"
        private String email; // "alice@example.com"
        private String phone; // "+84 901 234 567"
        private String address; // "District 1, Ho Chi Minh City"
        private Integer totalBookings;
        private String totalSpent; // "$3,450.00"
        private BigDecimal totalSpentValue;
        private Integer points;
        private String status; // "Active"
        private String avatarUrl; // "/team/avatar-1.png"
        private String residenceCardNumber;
        private String driverLicenseNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateClientRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        private String phone;
        private String address;
        private String residenceCardNumber;
        private String driverLicenseNumber;
        private String avatarUrl;
    }
}
