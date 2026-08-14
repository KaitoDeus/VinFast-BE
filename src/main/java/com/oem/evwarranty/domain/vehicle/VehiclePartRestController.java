package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.common.annotation.Auditable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for managing serial parts installed on electric vehicles.
 * Base Path: /api/v1/sc/vehicles/{vehicleId}/parts
 * Access: SC_STAFF, SC_TECHNICIAN, ADMIN
 */
@RestController
@RequestMapping("/api/v1/sc/vehicles/{vehicleId}/parts")
@Tag(name = "Vehicle Parts Management", description = "Operations for tracking, installing, and replacing serial components (batteries, motors, inverters) on vehicles")
public class VehiclePartRestController {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    public VehiclePartRestController(VehicleService vehicleService, VehicleMapper vehicleMapper) {
        this.vehicleService = vehicleService;
        this.vehicleMapper = vehicleMapper;
    }

    @GetMapping
    @Operation(summary = "List installed parts", description = "Retrieve all components currently or previously installed on a specific vehicle")
    public ResponseEntity<List<VehiclePartDTO>> listParts(@PathVariable Long vehicleId) {
        List<VehiclePart> parts = vehicleService.findPartsByVehicleId(vehicleId);
        List<VehiclePartDTO> dtos = parts.stream()
                .map(vehicleMapper::toPartDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SC_STAFF', 'SC_TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Install serial part", description = "Register the installation of a serialized component (e.g. Battery Module, Drive Unit) with automatic warranty calculation")
    @Auditable(action = "INSTALL_PART", resourceType = "VEHICLE_PART")
    public ResponseEntity<VehiclePartDTO> installPart(
            @PathVariable Long vehicleId,
            @RequestBody InstallPartRequest request,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        VehiclePart installed = vehicleService.installPart(
                vehicleId,
                request.getPartId(),
                request.getSerialNumber(),
                request.getNotes(),
                username);

        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleMapper.toPartDTO(installed));
    }

    @GetMapping("/{vehiclePartId}")
    @Operation(summary = "Get installed part details", description = "Retrieve metadata and warranty validity for a specific installed part")
    public ResponseEntity<VehiclePartDTO> getPartDetails(
            @PathVariable Long vehicleId,
            @PathVariable Long vehiclePartId) {

        return vehicleService.findPartsByVehicleId(vehicleId).stream()
                .filter(vp -> vp.getId().equals(vehiclePartId))
                .findFirst()
                .map(vehicleMapper::toPartDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{vehiclePartId}")
    @PreAuthorize("hasAnyRole('SC_STAFF', 'ADMIN')")
    @Operation(summary = "Remove/replace part", description = "Mark an installed serial component as REPLACED during warranty repair")
    @Auditable(action = "REMOVE_PART", resourceType = "VEHICLE_PART")
    public ResponseEntity<Map<String, Object>> removePart(
            @PathVariable Long vehicleId,
            @PathVariable Long vehiclePartId) {

        VehiclePart removed = vehicleService.removePart(vehiclePartId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Part marked as REPLACED successfully",
                "vehiclePartId", removed.getId(),
                "partStatus", removed.getStatus().name()
        ));
    }

    /**
     * Request payload for installing a serialized part on a vehicle.
     */
    @Data
    public static class InstallPartRequest {
        private Long partId;
        private String serialNumber;
        private String notes;
    }
}
