package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.user.UserService;

import com.oem.evwarranty.domain.claim.WarrantyClaimDTO;
import com.oem.evwarranty.domain.claim.WarrantyClaimService;
import com.oem.evwarranty.domain.claim.WarrantyClaimMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Warranty Claim API", description = "Endpoints for managing warranty claims")
public class WarrantyClaimRestController {

    private final WarrantyClaimService claimService;
    private final WarrantyClaimMapper claimMapper;

    public WarrantyClaimRestController(WarrantyClaimService claimService, WarrantyClaimMapper claimMapper) {
        this.claimService = claimService;
        this.claimMapper = claimMapper;
    }

    @GetMapping
    @Operation(summary = "Get all claims with pagination")
    public ResponseEntity<Page<WarrantyClaimDTO>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<WarrantyClaimDTO> claims = claimService
                .findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(claimMapper::toDTO);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID")
    public ResponseEntity<WarrantyClaimDTO> getClaimById(@PathVariable Long id) {
        return claimService.findById(id)
                .map(claimMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search claims by number or VIN")
    public ResponseEntity<List<WarrantyClaimDTO>> searchClaims(@RequestParam String query) {
        List<WarrantyClaimDTO> results = claimService.searchClaims(query, PageRequest.of(0, 20))
                .getContent().stream()
                .map(claimMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending claims for approval")
    public ResponseEntity<Page<WarrantyClaimDTO>> getPendingClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<WarrantyClaimDTO> claims = claimService
                .findPendingClaims(PageRequest.of(page, size, Sort.by("createdAt").ascending()))
                .map(claimMapper::toDTO);
        return ResponseEntity.ok(claims);
    }
}



