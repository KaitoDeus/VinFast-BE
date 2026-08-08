package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Warranty Claims in Service Centers (/api/v1/sc/claims).
 */
@RestController
@RequestMapping("/api/v1/sc/claims")
@Tag(name = "Warranty Claim Management REST API", description = "Operations for handling warranty claims in Service Centers")
public class WarrantyClaimController {

    private final WarrantyClaimService claimService;
    private final UserService userService;

    public WarrantyClaimController(WarrantyClaimService claimService, UserService userService) {
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List warranty claims", description = "Retrieve a paginated list of warranty claims filtered by service center permissions")
    public ResponseEntity<Page<WarrantyClaim>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String search,
                                                     Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElse(null);
        Page<WarrantyClaim> claims;

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isEvmStaff = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EVM_STAFF"));

        if (isAdmin || isEvmStaff) {
            claims = claimService.searchClaims(search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            if (user != null && user.getServiceCenter() != null) {
                claims = claimService.findByServiceCenter(
                        user.getServiceCenter(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()));
            } else {
                claims = Page.empty(PageRequest.of(page, size));
            }
        }
        return ResponseEntity.ok(claims);
    }

    @PostMapping
    @Operation(summary = "Create claim", description = "Register a new warranty claim")
    public ResponseEntity<WarrantyClaim> create(@Valid @RequestBody WarrantyClaim claim,
                                                @RequestParam Long vehicleId,
                                                @RequestParam(required = false) Long vehiclePartId,
                                                Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElse(null);
        Long userId = user != null ? user.getId() : null;
        claim.setServiceCenter(user != null ? user.getServiceCenter() : null);

        WarrantyClaim created = claimService.createClaim(claim, vehicleId, vehiclePartId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim details", description = "Retrieve warranty claim details by ID")
    public ResponseEntity<WarrantyClaim> getById(@PathVariable Long id) {
        WarrantyClaim claim = claimService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        return ResponseEntity.ok(claim);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update claim", description = "Update an existing warranty claim")
    public ResponseEntity<WarrantyClaim> update(@PathVariable Long id, @Valid @RequestBody WarrantyClaim claim) {
        WarrantyClaim updated = claimService.updateClaim(id, claim);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit claim", description = "Submit a draft warranty claim for review by manufacturer")
    public ResponseEntity<WarrantyClaim> submit(@PathVariable Long id) {
        WarrantyClaim submitted = claimService.submitClaim(id);
        return ResponseEntity.ok(submitted);
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign technician", description = "Assign a technician to perform work on the claim")
    public ResponseEntity<WarrantyClaim> assignTechnician(@PathVariable Long id, @RequestParam Long technicianId) {
        WarrantyClaim assigned = claimService.assignTechnician(id, technicianId);
        return ResponseEntity.ok(assigned);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete claim", description = "Mark a warranty claim as completed after repair work")
    public ResponseEntity<WarrantyClaim> complete(@PathVariable Long id) {
        WarrantyClaim completed = claimService.completeClaim(id);
        return ResponseEntity.ok(completed);
    }
}
