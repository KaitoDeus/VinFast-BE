package com.oem.evwarranty.domain.inventory;


import com.oem.evwarranty.domain.inventory.Inventory;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.inventory.InventoryService;
import com.oem.evwarranty.domain.inventory.PartService;
import com.oem.evwarranty.domain.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for Inventory management (Service Center).
 */
@Controller
@RequestMapping("/sc/inventory")
@Tag(name = "Inventory Management", description = "Operations for tracking and adjusting part stock at Service Centers")
public class InventoryController {

    private final InventoryService inventoryService;
    private final PartService partService;
    private final UserService userService;

    public InventoryController(InventoryService inventoryService,
            PartService partService,
            UserService userService) {
        this.inventoryService = inventoryService;
        this.partService = partService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List inventory", description = "View available parts and stock levels at the current service center")
    public String list(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElse(null);
        List<Inventory> inventory;

        if (user != null && user.getServiceCenter() != null) {
            inventory = inventoryService.findByServiceCenter(user.getServiceCenter());
            model.addAttribute("lowStockItems",
                    inventoryService.findLowStockItemsByServiceCenter(user.getServiceCenter()));
        } else {
            inventory = inventoryService.findAll();
            model.addAttribute("lowStockItems", inventoryService.findLowStockItems());
        }

        model.addAttribute("inventory", inventory);
        model.addAttribute("parts", partService.findAllActive());
        return "sc/inventory/list";
    }

    @PostMapping("/add")
    @Operation(summary = "Add stock", description = "Increase inventory level for a specific part at the user's service center")
    public String addStock(@RequestParam Long partId,
            @RequestParam int quantity,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            String serviceCenter = user != null ? user.getServiceCenter() : "DEFAULT";
            if (partId == null)
                throw new IllegalArgumentException("Part ID cannot be null");
            inventoryService.createOrUpdateInventory(partId, serviceCenter, quantity);
            redirectAttributes.addFlashAttribute("success", "Stock added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sc/inventory";
    }

    @PostMapping("/{id}/adjust")
    public String adjustStock(@PathVariable Long id,
            @RequestParam int adjustment,
            RedirectAttributes redirectAttributes) {
        try {
            if (id == null)
                throw new IllegalArgumentException("ID cannot be null");
            inventoryService.adjustStock(id, adjustment);
            redirectAttributes.addFlashAttribute("success", "Stock adjusted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sc/inventory";
    }
}


