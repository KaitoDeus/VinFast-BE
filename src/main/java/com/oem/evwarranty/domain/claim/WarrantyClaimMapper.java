package com.oem.evwarranty.domain.claim;


import com.oem.evwarranty.domain.claim.WarrantyClaimDTO;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import org.springframework.stereotype.Component;

@Component
public class WarrantyClaimMapper {

    public WarrantyClaimDTO toDTO(WarrantyClaim claim) {
        if (claim == null)
            return null;

        WarrantyClaimDTO dto = new WarrantyClaimDTO();
        dto.setId(claim.getId());
        dto.setClaimNumber(claim.getClaimNumber());

        if (claim.getVehicle() != null) {
            dto.setVehicleVin(claim.getVehicle().getVin());
            dto.setVehicleModel(claim.getVehicle().getModel());
        }

        dto.setStatus(claim.getStatus() != null ? claim.getStatus().name() : "UNKNOWN");
        dto.setFailureDescription(claim.getFailureDescription());
        dto.setTotalCost(claim.getTotalCost());
        dto.setServiceCenter(claim.getServiceCenter());
        dto.setCreatedAt(claim.getCreatedAt());

        if (claim.getTechnician() != null) {
            dto.setTechnicianName(claim.getTechnician().getFullName());
        }

        return dto;
    }
}

