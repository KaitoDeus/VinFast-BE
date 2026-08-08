package com.oem.evwarranty.domain.claim;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WarrantyClaimDTO {
    private Long id;
    private String claimNumber;
    private String vehicleVin;
    private String vehicleModel;
    private String status;
    private String failureDescription;
    private BigDecimal totalCost;
    private String serviceCenter;
    private LocalDateTime createdAt;
    private String technicianName;
}


