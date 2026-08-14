package com.oem.evwarranty.domain.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for installed serial parts on a vehicle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePartDTO {
    private Long id;
    private String serialNumber;
    private Long partId;
    private String partName;
    private String partCode;
    private String partCategory;
    private Long vehicleId;
    private String vehicleVin;
    private LocalDate installationDate;
    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private Boolean underWarranty;
    private String status;
    private Long installedById;
    private String installedByName;
    private String notes;
}
