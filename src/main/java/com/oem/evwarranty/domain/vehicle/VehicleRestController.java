package com.oem.evwarranty.domain.vehicle;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-Only REST Controller for general authenticated vehicle queries.
 * Base Path: /api/v1/vehicles
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicle Query API", description = "Read-only endpoints for vehicle search, metadata inspection, and installed parts")
public class VehicleRestController {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    public VehicleRestController(VehicleService vehicleService, VehicleMapper vehicleMapper) {
        this.vehicleService = vehicleService;
        this.vehicleMapper = vehicleMapper;
    }

    @GetMapping
    @Operation(summary = "Get paginated list of vehicles")
    public ResponseEntity<Page<VehicleDTO>> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<VehicleDTO> vehicles = vehicleService.findAll(PageRequest.of(page, size))
                .map(vehicleMapper::toDTO);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle details by ID")
    public ResponseEntity<VehicleDTO> getVehicle(@PathVariable Long id) {
        return vehicleService.findById(id)
                .map(vehicleMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search vehicle by VIN")
    public ResponseEntity<VehicleDTO> searchByVin(@RequestParam String vin) {
        return vehicleService.findByVin(vin)
                .map(vehicleMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/parts")
    @Operation(summary = "Get installed parts for a vehicle")
    public ResponseEntity<List<VehiclePartDTO>> getVehicleParts(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        List<VehiclePart> parts = vehicleService.findPartsByVehicleId(id);
        List<VehiclePartDTO> dtos = parts.stream()
                .map(vehicleMapper::toPartDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
