package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.vehicle.VehiclePartRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.audit.AuditLogService;
import com.oem.evwarranty.domain.vehicle.VehiclePart;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;
import com.oem.evwarranty.domain.user.UserRepository;

import com.oem.evwarranty.domain.vehicle.Vehicle;

import com.oem.evwarranty.common.exception.BusinessLogicException;
import com.oem.evwarranty.common.exception.ResourceNotFoundException;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for Warranty Claim management operations.
 */
@Service
@Transactional
public class WarrantyClaimService {

    private final WarrantyClaimRepository claimRepository;
    private final VehicleRepository vehicleRepository;
    private final VehiclePartRepository vehiclePartRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public WarrantyClaimService(WarrantyClaimRepository claimRepository,
            VehicleRepository vehicleRepository,
            VehiclePartRepository vehiclePartRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.claimRepository = claimRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehiclePartRepository = vehiclePartRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<WarrantyClaim> findAll() {
        return claimRepository.findAll();
    }

    public Optional<WarrantyClaim> findById(@NonNull Long id) {
        return claimRepository.findById(id);
    }

    public Optional<WarrantyClaim> findByClaimNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber);
    }

    public Page<WarrantyClaim> findAll(@NonNull Pageable pageable) {
        return claimRepository.findAll(pageable);
    }

    public Page<WarrantyClaim> searchClaims(String search, @NonNull Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return claimRepository.findAll(pageable);
        }
        return claimRepository.searchClaims(search, pageable);
    }

    public List<WarrantyClaim> findByStatus(WarrantyClaim.ClaimStatus status) {
        return claimRepository.findByStatus(status);
    }

    public List<WarrantyClaim> findByVehicleId(Long vehicleId) {
        return claimRepository.findByVehicleId(vehicleId);
    }

    public Page<WarrantyClaim> findByServiceCenter(String serviceCenter, Pageable pageable) {
        return claimRepository.findByServiceCenterPaged(serviceCenter, pageable);
    }

    public Page<WarrantyClaim> findPendingClaims(Pageable pageable) {
        return claimRepository.findByStatusIn(
                Arrays.asList(WarrantyClaim.ClaimStatus.SUBMITTED, WarrantyClaim.ClaimStatus.UNDER_REVIEW),
                pageable);
    }

    public WarrantyClaim createClaim(WarrantyClaim claim, @NonNull Long vehicleId, Long vehiclePartId,
            Long submittedById) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        claim.setVehicle(vehicle);

        if (vehiclePartId != null) {
            VehiclePart vehiclePart = vehiclePartRepository.findById(vehiclePartId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Vehicle part not found with ID: " + vehiclePartId));
            claim.setVehiclePart(vehiclePart);
        }

        User submitter = null;
        if (submittedById != null) {
            submitter = userRepository.findById(submittedById)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + submittedById));
            claim.setSubmittedBy(submitter);
        }

        claim.setStatus(WarrantyClaim.ClaimStatus.DRAFT);
        claim.setClaimNumber("WC" + System.currentTimeMillis());

        WarrantyClaim saved = claimRepository.save(claim);
        auditLogService.log(submitter != null ? submitter.getUsername() : "SYSTEM",
                "CREATE", "WARRANTY_CLAIM", saved.getId(),
                "Created new claim for vehicle: " + vehicle.getVin());
        return saved;
    }

    public WarrantyClaim submitClaim(@NonNull Long id) {
        return claimRepository.findById(id)
                .map(claim -> {
                    if (claim.getStatus() != WarrantyClaim.ClaimStatus.DRAFT) {
                        throw new BusinessLogicException(
                                "Claim can only be submitted from DRAFT status. Current status: " + claim.getStatus());
                    }
                    claim.setStatus(WarrantyClaim.ClaimStatus.SUBMITTED);
                    claim.setSubmittedAt(LocalDateTime.now());

                    WarrantyClaim saved = claimRepository.save(claim);
                    auditLogService.log(
                            claim.getSubmittedBy() != null ? claim.getSubmittedBy().getUsername() : "SYSTEM",
                            "SUBMIT", "WARRANTY_CLAIM", saved.getId(), "Submitted claim for review");
                    return saved;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));
    }

    public WarrantyClaim approveClaim(@NonNull Long id, @NonNull Long reviewerId) {
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with ID: " + reviewerId));

        return claimRepository.findById(id)
                .map(claim -> {
                    claim.setStatus(WarrantyClaim.ClaimStatus.APPROVED);
                    claim.setReviewedBy(reviewer);
                    claim.setReviewedAt(LocalDateTime.now());

                    WarrantyClaim saved = claimRepository.save(claim);
                    auditLogService.log(reviewer.getUsername(), "APPROVE", "WARRANTY_CLAIM", saved.getId(),
                            "Approved the claim");
                    return saved;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));
    }

    public WarrantyClaim rejectClaim(@NonNull Long id, @NonNull Long reviewerId, String reason) {
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with ID: " + reviewerId));

        return claimRepository.findById(id)
                .map(claim -> {
                    claim.setStatus(WarrantyClaim.ClaimStatus.REJECTED);
                    claim.setReviewedBy(reviewer);
                    claim.setReviewedAt(LocalDateTime.now());
                    claim.setRejectionReason(reason);

                    WarrantyClaim saved = claimRepository.save(claim);
                    auditLogService.log(reviewer.getUsername(), "REJECT", "WARRANTY_CLAIM", saved.getId(),
                            "Rejected. Reason: " + reason);
                    return saved;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));
    }

    public WarrantyClaim completeClaim(@NonNull Long id) {
        return claimRepository.findById(id)
                .map(claim -> {
                    claim.setStatus(WarrantyClaim.ClaimStatus.COMPLETED);
                    claim.setCompletedAt(LocalDateTime.now());

                    BigDecimal laborCost = claim.getLaborCost() != null ? claim.getLaborCost() : BigDecimal.ZERO;
                    BigDecimal partsCost = claim.getPartsCost() != null ? claim.getPartsCost() : BigDecimal.ZERO;
                    claim.setTotalCost(laborCost.add(partsCost));

                    WarrantyClaim saved = claimRepository.save(claim);
                    auditLogService.log("SYSTEM", "COMPLETE", "WARRANTY_CLAIM", saved.getId(),
                            "Claim marked as completed");
                    return saved;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));
    }

    public WarrantyClaim updateClaim(@NonNull Long id, WarrantyClaim updatedClaim) {
        return claimRepository.findById(id)
                .map(claim -> {
                    claim.setFailureDescription(updatedClaim.getFailureDescription());
                    claim.setDiagnosisNotes(updatedClaim.getDiagnosisNotes());
                    claim.setRepairDescription(updatedClaim.getRepairDescription());
                    claim.setLaborHours(updatedClaim.getLaborHours());
                    claim.setLaborCost(updatedClaim.getLaborCost());
                    claim.setPartsCost(updatedClaim.getPartsCost());
                    claim.setMileageAtClaim(updatedClaim.getMileageAtClaim());

                    WarrantyClaim saved = claimRepository.save(claim);
                    auditLogService.log("SYSTEM", "UPDATE", "WARRANTY_CLAIM", saved.getId(),
                            "Updated claim technical details");
                    return saved;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));
    }

    public WarrantyClaim assignTechnician(@NonNull Long claimId, @NonNull Long technicianId) {
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + technicianId));

        return claimRepository.findById(claimId)
                .map(claim -> {
                    claim.setTechnician(technician);
                    claim.setStatus(WarrantyClaim.ClaimStatus.IN_PROGRESS);

                    WarrantyClaim saved = claimRepository.save(claim);
                    auditLogService.log("SYSTEM", "ASSIGN_TECH", "WARRANTY_CLAIM", saved.getId(),
                            "Assigned technician: " + technician.getFullName());
                    return saved;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));
    }

    public void deleteClaim(@NonNull Long id) {
        if (!claimRepository.existsById(id)) {
            throw new ResourceNotFoundException("Claim not found with ID: " + id);
        }
        claimRepository.deleteById(id);
        auditLogService.log("SYSTEM", "DELETE", "WARRANTY_CLAIM", id, "Permanent deletion");
    }

    public long count() {
        return claimRepository.count();
    }

    public long countByStatus(WarrantyClaim.ClaimStatus status) {
        return claimRepository.countByStatus(status);
    }
}









