package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.domain.vehicle.Vehicle;


import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.claim.WarrantyClaimService;
import com.oem.evwarranty.domain.vehicle.VehicleService;
import com.oem.evwarranty.domain.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for Warranty Claims (Service Center).
 */
@Controller
@RequestMapping("/sc/claims")
@Tag(name = "Warranty Claim Management", description = "Operations for handling warranty claims in Service Centers")
public class WarrantyClaimController {

    private final WarrantyClaimService claimService;
    private final VehicleService vehicleService;
    private final UserService userService;

    public WarrantyClaimController(WarrantyClaimService claimService,
            VehicleService vehicleService,
            UserService userService) {
        this.claimService = claimService;
        this.vehicleService = vehicleService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List warranty claims", description = "Retrieve a paginated list of warranty claims filters by search query and user permissions")
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication auth) {

        User user = userService.findByUsername(auth.getName()).orElse(null);
        Page<WarrantyClaim> claims;

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isEvmStaff = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EVM_STAFF"));

        if (isAdmin || isEvmStaff) {
            claims = claimService.searchClaims(
                    search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            if (user != null && user.getServiceCenter() != null) {
                claims = claimService.findByServiceCenter(
                        user.getServiceCenter(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()));
            } else {
                claims = Page.empty(PageRequest.of(page, size));
            }
        }

        model.addAttribute("claims", claims);
        model.addAttribute("search", search);
        model.addAttribute("statuses", WarrantyClaim.ClaimStatus.values());
        return "sc/claims/list";
    }

    @GetMapping("/new")
    @Operation(summary = "New claim form", description = "Show the form to create a new warranty claim")
    public String createForm(Model model) {
        model.addAttribute("claim", new WarrantyClaim());
        model.addAttribute("vehicles", vehicleService.findAll());
        return "sc/claims/form";
    }

    @PostMapping
    @Operation(summary = "Create claim", description = "Register a new warranty claim for a vehicle")
    public String create(@Valid @ModelAttribute WarrantyClaim claim,
            BindingResult result,
            @RequestParam Long vehicleId,
            @RequestParam(required = false) Long vehiclePartId,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("vehicles", vehicleService.findAll());
            return "sc/claims/form";
        }
        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            Long userId = user != null ? user.getId() : null;
            claim.setServiceCenter(user != null ? user.getServiceCenter() : null);

            if (vehicleId == null)
                throw new IllegalArgumentException("Vehicle ID cannot be null");
            claimService.createClaim(claim, vehicleId, vehiclePartId, userId);
            redirectAttributes.addFlashAttribute("success", "Warranty claim created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("vehicles", vehicleService.findAll());
            return "sc/claims/form";
        }
        return "redirect:/sc/claims";
    }

    @GetMapping("/{id}")
    @Operation(summary = "View claim details", description = "Display full details and history of a specific warranty claim")
    public String view(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        WarrantyClaim claim = claimService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        model.addAttribute("claim", claim);
        model.addAttribute("technicians", userService.findTechnicians());
        return "sc/claims/view";
    }

    @GetMapping("/{id}/edit")
    @Operation(summary = "Edit claim form", description = "Show the form to edit an existing draft claim")
    public String editForm(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        WarrantyClaim claim = claimService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        model.addAttribute("claim", claim);
        model.addAttribute("vehicles", vehicleService.findAll());
        return "sc/claims/form";
    }

    @PostMapping("/{id}")
    @Operation(summary = "Update claim", description = "Save changes to an existing warranty claim")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute WarrantyClaim claim,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "sc/claims/form";
        }
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            claimService.updateClaim(id, claim);
            redirectAttributes.addFlashAttribute("success", "Claim updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sc/claims/" + id;
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit claim", description = "Submit a draft warranty claim for review by the manufacturer")
    public String submit(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            claimService.submitClaim(id);
            redirectAttributes.addFlashAttribute("success", "Claim submitted for review");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sc/claims/" + id;
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign technician", description = "Assign a technician to perform work on the warranty claim")
    public String assignTechnician(@PathVariable Long id,
            @RequestParam Long technicianId,
            RedirectAttributes redirectAttributes) {
        try {
            if (id == null || technicianId == null)
                throw new IllegalArgumentException("IDs cannot be null");
            claimService.assignTechnician(id, technicianId);
            redirectAttributes.addFlashAttribute("success", "Technician assigned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sc/claims/" + id;
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete claim", description = "Mark a warranty claim as completed after repair work is done")
    public String complete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            claimService.completeClaim(id);
            redirectAttributes.addFlashAttribute("success", "Claim completed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sc/claims/" + id;
    }

    @GetMapping("/api/search")
    @ResponseBody
    @Operation(summary = "Search claims API", description = "JSON endpoint to search claims by number or VIN (AJAX support)")
    public Page<WarrantyClaim> searchApi(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimService.searchClaims(search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }
}


