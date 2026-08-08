package com.oem.evwarranty.domain.campaign;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.user.UserService;

import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import com.oem.evwarranty.domain.campaign.ServiceCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
@Tag(name = "Campaign API", description = "Endpoints for managing service campaigns (recalls)")
public class CampaignRestController {

    private final ServiceCampaignService campaignService;

    public CampaignRestController(ServiceCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    @Operation(summary = "Get all campaigns with pagination")
    public ResponseEntity<Page<ServiceCampaign>> getAllCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ServiceCampaign> campaigns = campaignService.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(campaigns);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active campaigns")
    public ResponseEntity<List<ServiceCampaign>> getActiveCampaigns() {
        return ResponseEntity.ok(campaignService.findActiveCampaigns());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campaign by ID")
    public ResponseEntity<ServiceCampaign> getCampaignById(@PathVariable Long id) {
        return campaignService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}



