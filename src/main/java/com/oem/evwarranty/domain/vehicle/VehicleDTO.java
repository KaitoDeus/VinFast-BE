package com.oem.evwarranty.domain.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for Vehicle representations across the REST API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private Long id;
    private String vin;
    private String model;
    private String make;
    private Integer year;
    private String color;
    private String batteryType;
    private Double batteryCapacity;
    private String motorType;
    private Integer mileage;
    private LocalDate manufactureDate;
    private LocalDate purchaseDate;
    private LocalDate registrationDate;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private Boolean underWarranty;
    private String status;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
}
