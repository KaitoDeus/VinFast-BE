package com.oem.evwarranty.domain.vehicle;

import com.oem.evwarranty.domain.customer.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Vehicle management (/api/v1/sc/vehicles).
 */
@RestController
@RequestMapping("/api/v1/sc/vehicles")
@Tag(name = "Vehicle Management REST API", description = "Operations for registering and managing electric vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final CustomerService customerService;

    public VehicleController(VehicleService vehicleService, CustomerService customerService) {
        this.vehicleService = vehicleService;
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "List vehicles", description = "View a paginated list of all vehicles in the system")
    public ResponseEntity<Page<Vehicle>> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) String search) {
        Page<Vehicle> vehicles = vehicleService.searchVehicles(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    @Operation(summary = "Create vehicle", description = "Register a new vehicle")
    public ResponseEntity<Vehicle> create(@Valid @RequestBody Vehicle vehicle,
                                          @RequestParam(required = false) Long customerId) {
        Vehicle created = vehicleService.createVehicle(vehicle, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle details", description = "Retrieve vehicle by ID")
    public ResponseEntity<Vehicle> getById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        return ResponseEntity.ok(vehicle);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Update an existing vehicle")
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        Vehicle updated = vehicleService.updateVehicle(id, vehicle);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "Delete a vehicle by ID")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Vehicle deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search by VIN", description = "Find vehicle details by VIN")
    public ResponseEntity<Vehicle> searchByVin(@RequestParam String vin) {
        return vehicleService.findByVin(vin)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
