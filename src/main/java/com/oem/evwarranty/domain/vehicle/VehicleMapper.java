package com.oem.evwarranty.domain.vehicle;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper component for converting Vehicle & VehiclePart entities to DTOs.
 */
@Component
public class VehicleMapper {

    public VehicleDTO toDTO(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        String displayName = vehicle.getModelName() != null ? vehicle.getModelName() : (vehicle.getModel() != null ? vehicle.getModel() : "VinFast EV");
        String formattedId = "VF-" + String.format("%03d", vehicle.getId() != null ? vehicle.getId() : 1);
        String dailyPriceStr = vehicle.getDailyPrice() != null ? "$" + vehicle.getDailyPrice().intValue() : "$120";
        String statusStr = vehicle.getFleetStatus() != null ? formatStatus(vehicle.getFleetStatus().name()) : (vehicle.getStatus() != null ? formatStatus(vehicle.getStatus().name()) : "Available");
        String rangeStr = (vehicle.getRangeKm() != null ? vehicle.getRangeKm() : 450) + " km";
        String batteryFuelStr = (vehicle.getBatteryCapacity() != null ? vehicle.getBatteryCapacity() + " kWh" : "87.7 kWh") +
                " (" + (vehicle.getBatteryFuelPercent() != null ? vehicle.getBatteryFuelPercent() : 90) + "%)";
        String topSpeedStr = (vehicle.getTopSpeedKmh() != null ? vehicle.getTopSpeedKmh() : 200) + " km/h";
        String accelStr = vehicle.getAccelerationSpec() != null ? vehicle.getAccelerationSpec() : "5.5s (0-100km/h)";
        String imageStr = vehicle.getHeroImageUrl() != null ? vehicle.getHeroImageUrl() : "/cars/vf8.png";

        VehicleDTO.VehicleDTOBuilder builder = VehicleDTO.builder()
                .id(vehicle.getId())
                .formattedId(formattedId)
                .vin(vehicle.getVin())
                .brand(vehicle.getBrand() != null ? vehicle.getBrand() : "VinFast")
                .model(vehicle.getModel() != null ? vehicle.getModel() : displayName)
                .modelName(displayName)
                .make(vehicle.getMake() != null ? vehicle.getMake() : "VinFast")
                .carType(vehicle.getCarType() != null ? vehicle.getCarType().name() : "SUV")
                .licensePlate(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "30A-888.88")
                .dailyPrice(dailyPriceStr)
                .priceValue(vehicle.getDailyPrice() != null ? vehicle.getDailyPrice() : BigDecimal.valueOf(120.0))
                .year(vehicle.getYear() != null ? vehicle.getYear() : 2024)
                .color(vehicle.getColor() != null ? vehicle.getColor() : "Jet Black")
                .transmission(vehicle.getTransmission() != null ? vehicle.getTransmission() : "Automatic")
                .capacity(vehicle.getCapacity() != null ? vehicle.getCapacity() : "5 seats")
                .range(rangeStr)
                .rangeKm(vehicle.getRangeKm() != null ? vehicle.getRangeKm() : 450)
                .batteryType(vehicle.getBatteryType() != null ? vehicle.getBatteryType() : "CATL Lithium-ion NMC")
                .batteryCapacity(vehicle.getBatteryCapacity() != null ? vehicle.getBatteryCapacity() : 87.7)
                .batteryFuel(batteryFuelStr)
                .batteryFuelPercent(vehicle.getBatteryFuelPercent() != null ? vehicle.getBatteryFuelPercent() : 90)
                .topSpeed(topSpeedStr)
                .topSpeedKmh(vehicle.getTopSpeedKmh() != null ? vehicle.getTopSpeedKmh() : 200)
                .acceleration(accelStr)
                .accelerationSpec(accelStr)
                .image(imageStr)
                .heroImageUrl(imageStr)
                .description(vehicle.getDescription() != null ? vehicle.getDescription() : "Mẫu xe điện thể thao đột phá của VinFast.")
                .unitsCount(vehicle.getUnitsCount() != null ? vehicle.getUnitsCount() : 10)
                .motorType(vehicle.getMotorType())
                .mileage(vehicle.getMileage() != null ? vehicle.getMileage() : 15000)
                .manufactureDate(vehicle.getManufactureDate())
                .purchaseDate(vehicle.getPurchaseDate())
                .registrationDate(vehicle.getRegistrationDate())
                .warrantyStartDate(vehicle.getWarrantyStartDate())
                .warrantyEndDate(vehicle.getWarrantyEndDate())
                .underWarranty(vehicle.isUnderWarranty())
                .status(statusStr);

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

    private String formatStatus(String statusName) {
        if (statusName == null) return "Available";
        switch (statusName.toUpperCase()) {
            case "AVAILABLE": case "ACTIVE": return "Available";
            case "MAINTENANCE": return "Maintenance";
            case "UNAVAILABLE": case "INACTIVE": return "Unavailable";
            case "RENTED": return "Rented";
            default: return statusName;
        }
    }
}
