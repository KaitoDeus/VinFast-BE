package com.oem.evwarranty.domain.inventory;

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
 * REST Controller for Part catalog management (/api/v1/evm/parts).
 */
@RestController
@RequestMapping("/api/v1/evm/parts")
@Tag(name = "Part Catalog REST API", description = "Operations for managing the master catalog of electric vehicle parts and components")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping
    @Operation(summary = "List parts", description = "View a paginated list of all parts in the master catalog")
    public ResponseEntity<Page<Part>> list(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String search) {
        Page<Part> parts = partService.searchParts(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(parts);
    }

    @PostMapping
    @Operation(summary = "Create part", description = "Add a new part to the master catalog")
    public ResponseEntity<Part> create(@Valid @RequestBody Part part) {
        Part created = partService.createPart(part);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get part details", description = "Retrieve part details by ID")
    public ResponseEntity<Part> getById(@PathVariable Long id) {
        Part part = partService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        return ResponseEntity.ok(part);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update part", description = "Update an existing part in the catalog")
    public ResponseEntity<Part> update(@PathVariable Long id, @Valid @RequestBody Part part) {
        Part updated = partService.updatePart(id, part);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/toggle")
    @Operation(summary = "Toggle part status", description = "Enable or disable a part in the catalog")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable Long id) {
        partService.togglePartStatus(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Part status updated successfully"));
    }
}
