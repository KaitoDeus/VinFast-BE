package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.claim.WarrantyClaimService;
import com.oem.evwarranty.domain.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for EVM Staff to review and approve/reject warranty claims.
 */
@Controller
@RequestMapping("/evm/claims")
@Tag(name = "Manufacturer Claim Review", description = "Operations for EV Manufacturer staff to review and approve/reject warranty claims")
public class EvmClaimController {

    private final WarrantyClaimService claimService;
    private final UserService userService;

    public EvmClaimController(WarrantyClaimService claimService, UserService userService) {
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List pending claims", description = "Retrieve a list of warranty claims waiting for review by manufacturer staff")
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        Page<WarrantyClaim> claims;
        if (status != null && !status.isEmpty()) {
            claims = claimService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                    .map(c -> c); // Filter by status in view
        } else {
            claims = claimService.findPendingClaims(
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }

        model.addAttribute("claims", claims);
        model.addAttribute("search", search);
        model.addAttribute("statuses", WarrantyClaim.ClaimStatus.values());
        return "evm/claims/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        WarrantyClaim claim = claimService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        model.addAttribute("claim", claim);
        return "evm/claims/view";
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve claim", description = "Approve a warranty claim for payment and repair")
    public String approve(@PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            Long reviewerId = userService.findByUsername(auth.getName())
                    .map(u -> u.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Reviewer ID not found"));
            claimService.approveClaim(id, reviewerId);
            redirectAttributes.addFlashAttribute("success", "Claim approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evm/claims/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
            @RequestParam String rejectionReason,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            Long reviewerId = userService.findByUsername(auth.getName())
                    .map(u -> u.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Reviewer ID not found"));
            claimService.rejectClaim(id, reviewerId, rejectionReason);
            redirectAttributes.addFlashAttribute("success", "Claim rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evm/claims/" + id;
    }
}



