package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for EVM Staff to review and approve/reject warranty claims.
 */
@RestController
@RequestMapping("/api/v1/evm/claims")
@Tag(name = "Manufacturer Claim Review REST API", description = "Operations for EV Manufacturer staff to review and approve/reject warranty claims")
public class EvmClaimController {

    private final WarrantyClaimService claimService;
    private final UserService userService;

    public EvmClaimController(WarrantyClaimService claimService, UserService userService) {
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List pending claims", description = "Retrieve a list of warranty claims waiting for review by manufacturer staff")
    public ResponseEntity<Page<WarrantyClaim>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String status) {
        Page<WarrantyClaim> claims;
        if (status != null && !status.isEmpty()) {
            claims = claimService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            claims = claimService.findPendingClaims(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim details", description = "Retrieve claim details by ID")
    public ResponseEntity<WarrantyClaim> getById(@PathVariable Long id) {
        WarrantyClaim claim = claimService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        return ResponseEntity.ok(claim);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve claim", description = "Approve a warranty claim for payment and repair")
    public ResponseEntity<WarrantyClaim> approve(@PathVariable Long id, Authentication auth) {
        Long reviewerId = userService.findByUsername(auth.getName()).map(User::getId).orElse(null);
        WarrantyClaim approved = claimService.approveClaim(id, reviewerId);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject claim", description = "Reject a warranty claim with a specified reason")
    public ResponseEntity<WarrantyClaim> reject(@PathVariable Long id, @RequestParam String rejectionReason, Authentication auth) {
        Long reviewerId = userService.findByUsername(auth.getName()).map(User::getId).orElse(null);
        WarrantyClaim rejected = claimService.rejectClaim(id, reviewerId, rejectionReason);
        return ResponseEntity.ok(rejected);
    }
}
