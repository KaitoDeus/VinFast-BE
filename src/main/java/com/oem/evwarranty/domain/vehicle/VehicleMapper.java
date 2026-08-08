package com.oem.evwarranty.domain.vehicle;


import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.vehicle.VehicleDTO;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleDTO toDTO(Vehicle vehicle) {
        if (vehicle == null)
            return null;

        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setVin(vehicle.getVin());
        dto.setModel(vehicle.getModel());
        dto.setMake(vehicle.getMake());
        dto.setYear(vehicle.getYear());
        dto.setColor(vehicle.getColor());
        dto.setStatus(vehicle.getStatus() != null ? vehicle.getStatus().name() : "UNKNOWN");

        if (vehicle.getCustomer() != null) {
            dto.setCustomerName(vehicle.getCustomer().getFullName());
        }

        dto.setWarrantyEndDate(vehicle.getWarrantyEndDate());
        dto.setMileage(vehicle.getMileage());

        return dto;
    }
}

