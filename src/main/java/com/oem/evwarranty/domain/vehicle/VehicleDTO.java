package com.oem.evwarranty.domain.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Vehicle representations across the VinFast EV Platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private Long id;
    private String formattedId;
    private String vin;
    private String brand;
    private String model;
    private String modelName;
    private String make;
    private String carType;
    private String licensePlate;
    private String dailyPrice;
    private BigDecimal priceValue;
    private Integer year;
    private String color;
    private String transmission;
    private String capacity;
    private String range;
    private Integer rangeKm;
    private String batteryType;
    private Double batteryCapacity;
    private String batteryFuel;
    private Integer batteryFuelPercent;
    private String topSpeed;
    private Integer topSpeedKmh;
    private String acceleration;
    private String accelerationSpec;
    private String image;
    private String heroImageUrl;
    private String description;
    private Integer unitsCount;
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
