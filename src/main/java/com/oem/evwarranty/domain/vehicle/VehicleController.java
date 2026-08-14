package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.common.annotation.Auditable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Vehicle management by Service Center staff.
 * Base Path: /api/v1/sc/vehicles
 * Access: SC_STAFF, ADMIN
 */
@RestController
@RequestMapping("/api/v1/sc/vehicles")
@Tag(name = "SC Vehicle Management", description = "Service Center APIs for vehicle registration, mileage updates, and lifecycle operations")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    public VehicleController(VehicleService vehicleService, VehicleMapper vehicleMapper) {
        this.vehicleService = vehicleService;
        this.vehicleMapper = vehicleMapper;
    }

    @GetMapping
    @Operation(summary = "List vehicles", description = "View a paginated list of all vehicles with optional search by VIN/model/make")
    public ResponseEntity<Page<VehicleDTO>> list(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String search) {
        Page<VehicleDTO> vehicles = vehicleService.searchVehicles(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(vehicleMapper::toDTO);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SC_STAFF', 'ADMIN')")
    @Operation(summary = "Register vehicle", description = "Register a new electric vehicle in the system with optional customer association")
    @Auditable(action = "CREATE", resourceType = "VEHICLE")
    public ResponseEntity<VehicleDTO> create(@Valid @RequestBody Vehicle vehicle,
                                              @RequestParam(required = false) Long customerId) {
        Vehicle created = vehicleService.createVehicle(vehicle, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleMapper.toDTO(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle details", description = "Retrieve complete vehicle details and warranty status by vehicle ID")
    public ResponseEntity<VehicleDTO> getById(@PathVariable Long id) {
        return vehicleService.findById(id)
                .map(vehicleMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SC_STAFF', 'ADMIN')")
    @Operation(summary = "Update vehicle", description = "Update technical specifications and metadata of an existing vehicle")
    @Auditable(action = "UPDATE", resourceType = "VEHICLE")
    public ResponseEntity<VehicleDTO> update(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        Vehicle updated = vehicleService.updateVehicle(id, vehicle);
        return ResponseEntity.ok(vehicleMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete vehicle", description = "Permanently remove a vehicle record (ADMIN only)")
    @Auditable(action = "DELETE", resourceType = "VEHICLE")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Vehicle deleted successfully",
                "vehicleId", id
        ));
    }

    @GetMapping("/search")
    @Operation(summary = "Search by VIN", description = "Locate an electric vehicle by its unique 17-character VIN")
    public ResponseEntity<VehicleDTO> searchByVin(@RequestParam String vin) {
        return vehicleService.findByVin(vin)
                .map(vehicleMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/mileage")
    @PreAuthorize("hasAnyRole('SC_STAFF', 'SC_TECHNICIAN', 'ADMIN')")
    @Operation(summary = "Update odometer mileage", description = "Update the vehicle odometer reading in kilometers")
    @Auditable(action = "UPDATE_MILEAGE", resourceType = "VEHICLE")
    public ResponseEntity<VehicleDTO> updateMileage(@PathVariable Long id,
                                                     @RequestBody Map<String, Integer> body) {
        Integer newMileage = body.get("mileage");
        if (newMileage == null) {
            return ResponseEntity.badRequest().build();
        }
        Vehicle updated = vehicleService.updateMileage(id, newMileage);
        return ResponseEntity.ok(vehicleMapper.toDTO(updated));
    }

    @GetMapping("/{id}/warranty-status")
    @Operation(summary = "Check warranty status", description = "Verify whether the vehicle is currently covered under manufacturer warranty")
    public ResponseEntity<Map<String, Object>> getWarrantyStatus(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));

        return ResponseEntity.ok(Map.of(
                "vehicleId", vehicle.getId(),
                "vin", vehicle.getVin(),
                "underWarranty", vehicle.isUnderWarranty(),
                "warrantyStartDate", vehicle.getWarrantyStartDate() != null ? vehicle.getWarrantyStartDate().toString() : "",
                "warrantyEndDate", vehicle.getWarrantyEndDate() != null ? vehicle.getWarrantyEndDate().toString() : "",
                "currentMileage", vehicle.getMileage() != null ? vehicle.getMileage() : 0
        ));
    }
}
