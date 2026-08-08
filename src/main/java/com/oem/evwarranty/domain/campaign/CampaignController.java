package com.oem.evwarranty.domain.campaign;

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

import java.util.Map;

/**
 * REST Controller for Service Campaign (Recall) management (EVM Staff).
 */
@RestController
@RequestMapping("/api/v1/evm/campaigns")
@Tag(name = "Service Campaigns REST API", description = "Operations for managing vehicle recalls and service bulletins by EV Manufacturer staff")
public class CampaignController {

    private final ServiceCampaignService campaignService;
    private final UserService userService;

    public CampaignController(ServiceCampaignService campaignService, UserService userService) {
        this.campaignService = campaignService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List campaigns", description = "View a paginated list of all service campaigns/recalls")
    public ResponseEntity<Page<ServiceCampaign>> list(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) String search) {
        Page<ServiceCampaign> campaigns = campaignService.searchCampaigns(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(campaigns);
    }

    @PostMapping
    @Operation(summary = "Create campaign", description = "Create a new service campaign / recall")
    public ResponseEntity<ServiceCampaign> create(@Valid @RequestBody ServiceCampaign campaign, Authentication auth) {
        Long createdById = userService.findByUsername(auth.getName()).map(User::getId).orElse(null);
        ServiceCampaign created = campaignService.createCampaign(campaign, createdById);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campaign details", description = "Retrieve campaign details by ID")
    public ResponseEntity<ServiceCampaign> getById(@PathVariable Long id) {
        ServiceCampaign campaign = campaignService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        return ResponseEntity.ok(campaign);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update campaign", description = "Update an existing campaign")
    public ResponseEntity<ServiceCampaign> update(@PathVariable Long id, @Valid @RequestBody ServiceCampaign campaign) {
        ServiceCampaign updated = campaignService.updateCampaign(id, campaign);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate campaign", description = "Set campaign status to ACTIVE")
    public ResponseEntity<ServiceCampaign> activate(@PathVariable Long id) {
        ServiceCampaign campaign = campaignService.activateCampaign(id);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete campaign", description = "Set campaign status to COMPLETED")
    public ResponseEntity<ServiceCampaign> complete(@PathVariable Long id) {
        ServiceCampaign campaign = campaignService.completeCampaign(id);
        return ResponseEntity.ok(campaign);
    }
}
