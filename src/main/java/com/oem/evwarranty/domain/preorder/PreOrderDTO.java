package com.oem.evwarranty.domain.preorder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PreOrderDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id; // "PO-001"
        private Long numericId;
        private String fullName;
        private String phone;
        private String email;
        private String color;
        private String scooterModel;
        private String content;
        private String status; // "Pending", "Contacted", "Confirmed"
        private String createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Họ và tên không được để trống")
        private String fullName;

        @NotBlank(message = "Số điện thoại không được để trống")
        private String phone;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        private String email;

        private String color;

        @NotBlank(message = "Vui lòng chọn mẫu xe máy điện")
        private String scooterModel;

        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull(message = "Trạng thái không được để trống")
        private String status;
    }
}
