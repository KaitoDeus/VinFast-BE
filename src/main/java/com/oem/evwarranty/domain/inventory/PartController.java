package com.oem.evwarranty.domain.inventory;

import com.oem.evwarranty.domain.vehicle.Vehicle;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for Part management (EVM Staff).
 */
@Controller
@RequestMapping("/evm/parts")
@Tag(name = "Part Catalog", description = "Operations for managing the master catalog of electric vehicle parts and components")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping
    @Operation(summary = "List parts", description = "View a paginated list of all parts in the master catalog")
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Page<Part> parts = partService.searchParts(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("parts", parts);
        model.addAttribute("search", search);
        model.addAttribute("categories", Part.PartCategory.values());
        return "evm/parts/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("part", new Part());
        model.addAttribute("categories", Part.PartCategory.values());
        return "evm/parts/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Part part,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Part.PartCategory.values());
            return "evm/parts/form";
        }
        try {
            partService.createPart(part);
            redirectAttributes.addFlashAttribute("success", "Part created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("categories", Part.PartCategory.values());
            return "evm/parts/form";
        }
        return "redirect:/evm/parts";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        Part part = partService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        model.addAttribute("part", part);
        return "evm/parts/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        Part part = partService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));
        model.addAttribute("part", part);
        model.addAttribute("categories", Part.PartCategory.values());
        return "evm/parts/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute Part part,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Part.PartCategory.values());
            return "evm/parts/form";
        }
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            partService.updatePart(id, part);
            redirectAttributes.addFlashAttribute("success", "Part updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evm/parts";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        partService.togglePartStatus(id);
        redirectAttributes.addFlashAttribute("success", "Part status updated");
        return "redirect:/evm/parts";
    }
}




