package com.oem.evwarranty.domain.vehicle;


import lombok.Data;
import java.time.LocalDate;

@Data
public class VehicleDTO {
    private Long id;
    private String vin;
    private String model;
    private String make;
    private Integer year;
    private String color;
    private String status;
    private String customerName;
    private LocalDate warrantyEndDate;
    private Integer mileage;
}


