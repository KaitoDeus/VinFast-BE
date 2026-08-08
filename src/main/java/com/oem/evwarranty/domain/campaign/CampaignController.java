package com.oem.evwarranty.domain.campaign;

import com.oem.evwarranty.domain.vehicle.Vehicle;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import com.oem.evwarranty.domain.campaign.ServiceCampaignService;
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
 * Controller for Service Campaign (Recall) management (EVM Staff).
 */
@Controller
@RequestMapping("/evm/campaigns")
@Tag(name = "Service Campaigns", description = "Operations for managing vehicle recalls and service bulletins by EV Manufacturer staff")
public class CampaignController {

    private final ServiceCampaignService campaignService;
    private final UserService userService;

    public CampaignController(ServiceCampaignService campaignService, UserService userService) {
        this.campaignService = campaignService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List campaigns", description = "View a paginated list of all service campaigns/recalls")
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<ServiceCampaign> campaigns = campaignService.searchCampaigns(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("search", search);
        return "evm/campaigns/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("campaign", new ServiceCampaign());
        model.addAttribute("campaignTypes", ServiceCampaign.CampaignType.values());
        model.addAttribute("severityLevels", ServiceCampaign.SeverityLevel.values());
        return "evm/campaigns/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ServiceCampaign campaign,
            BindingResult result,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("campaignTypes", ServiceCampaign.CampaignType.values());
            model.addAttribute("severityLevels", ServiceCampaign.SeverityLevel.values());
            return "evm/campaigns/form";
        }
        try {
            Long createdById = userService.findByUsername(auth.getName())
                    .map(u -> u.getId())
                    .orElse(null);
            campaignService.createCampaign(campaign, createdById);
            redirectAttributes.addFlashAttribute("success", "Campaign created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("campaignTypes", ServiceCampaign.CampaignType.values());
            model.addAttribute("severityLevels", ServiceCampaign.SeverityLevel.values());
            return "evm/campaigns/form";
        }
        return "redirect:/evm/campaigns";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        ServiceCampaign campaign = campaignService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        model.addAttribute("campaign", campaign);
        return "evm/campaigns/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        ServiceCampaign campaign = campaignService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        model.addAttribute("campaign", campaign);
        model.addAttribute("campaignTypes", ServiceCampaign.CampaignType.values());
        model.addAttribute("severityLevels", ServiceCampaign.SeverityLevel.values());
        return "evm/campaigns/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute ServiceCampaign campaign,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("campaignTypes", ServiceCampaign.CampaignType.values());
            model.addAttribute("severityLevels", ServiceCampaign.SeverityLevel.values());
            return "evm/campaigns/form";
        }
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            campaignService.updateCampaign(id, campaign);
            redirectAttributes.addFlashAttribute("success", "Campaign updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evm/campaigns/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            campaignService.activateCampaign(id);
            redirectAttributes.addFlashAttribute("success", "Campaign activated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evm/campaigns/" + id;
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            campaignService.completeCampaign(id);
            redirectAttributes.addFlashAttribute("success", "Campaign completed");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evm/campaigns/" + id;
    }
}



