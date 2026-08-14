package com.oem.evwarranty.domain.vehicle;

import org.springframework.stereotype.Component;

/**
 * Mapper component for converting Vehicle & VehiclePart entities to DTOs.
 */
@Component
public class VehicleMapper {

    public VehicleDTO toDTO(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        VehicleDTO.VehicleDTOBuilder builder = VehicleDTO.builder()
                .id(vehicle.getId())
                .vin(vehicle.getVin())
                .model(vehicle.getModel())
                .make(vehicle.getMake())
                .year(vehicle.getYear())
                .color(vehicle.getColor())
                .batteryType(vehicle.getBatteryType())
                .batteryCapacity(vehicle.getBatteryCapacity())
                .motorType(vehicle.getMotorType())
                .mileage(vehicle.getMileage())
                .manufactureDate(vehicle.getManufactureDate())
                .purchaseDate(vehicle.getPurchaseDate())
                .registrationDate(vehicle.getRegistrationDate())
                .warrantyStartDate(vehicle.getWarrantyStartDate())
                .warrantyEndDate(vehicle.getWarrantyEndDate())
                .underWarranty(vehicle.isUnderWarranty())
                .status(vehicle.getStatus() != null ? vehicle.getStatus().name() : "ACTIVE");

        if (vehicle.getCustomer() != null) {
            builder.customerId(vehicle.getCustomer().getId())
                    .customerName(vehicle.getCustomer().getFullName())
                    .customerPhone(vehicle.getCustomer().getPhone())
                    .customerEmail(vehicle.getCustomer().getEmail());
        }

        return builder.build();
    }

    public VehiclePartDTO toPartDTO(VehiclePart vp) {
        if (vp == null) {
            return null;
        }

        VehiclePartDTO.VehiclePartDTOBuilder builder = VehiclePartDTO.builder()
                .id(vp.getId())
                .serialNumber(vp.getSerialNumber())
                .installationDate(vp.getInstallationDate())
                .warrantyStartDate(vp.getWarrantyStartDate())
                .warrantyEndDate(vp.getWarrantyEndDate())
                .underWarranty(vp.isUnderWarranty())
                .status(vp.getStatus() != null ? vp.getStatus().name() : "ACTIVE")
                .notes(vp.getNotes());

        if (vp.getPart() != null) {
            builder.partId(vp.getPart().getId())
                    .partName(vp.getPart().getName())
                    .partCode(vp.getPart().getPartNumber())
                    .partCategory(vp.getPart().getCategory() != null ? vp.getPart().getCategory().name() : null);
        }

        if (vp.getVehicle() != null) {
            builder.vehicleId(vp.getVehicle().getId())
                    .vehicleVin(vp.getVehicle().getVin());
        }

        if (vp.getInstalledBy() != null) {
            builder.installedById(vp.getInstalledBy().getId())
                    .installedByName(vp.getInstalledBy().getFullName() != null 
                            ? vp.getInstalledBy().getFullName() 
                            : vp.getInstalledBy().getUsername());
        }

        return builder.build();
    }
}
