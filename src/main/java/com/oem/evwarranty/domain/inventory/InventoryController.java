package com.oem.evwarranty.domain.inventory;

import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Inventory management (/api/v1/sc/inventory).
 */
@RestController
@RequestMapping("/api/v1/sc/inventory")
@Tag(name = "Inventory Management REST API", description = "Operations for tracking and adjusting part stock at Service Centers")
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
    public ResponseEntity<Map<String, Object>> list(Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElse(null);
        List<Inventory> inventory;
        List<Inventory> lowStock;

        if (user != null && user.getServiceCenter() != null) {
            inventory = inventoryService.findByServiceCenter(user.getServiceCenter());
            lowStock = inventoryService.findLowStockItemsByServiceCenter(user.getServiceCenter());
        } else {
            inventory = inventoryService.findAll();
            lowStock = inventoryService.findLowStockItems();
        }

        return ResponseEntity.ok(Map.of(
                "inventory", inventory,
                "lowStockItems", lowStock,
                "activeParts", partService.findAllActive()
        ));
    }

    @PostMapping("/add")
    @Operation(summary = "Add stock", description = "Increase inventory level for a specific part")
    public ResponseEntity<Inventory> addStock(@RequestParam Long partId,
                                              @RequestParam int quantity,
                                              Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElse(null);
        String serviceCenter = user != null && user.getServiceCenter() != null ? user.getServiceCenter() : "SC-HANOI-01";
        Inventory inventory = inventoryService.createOrUpdateInventory(partId, serviceCenter, quantity);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{id}/adjust")
    @Operation(summary = "Adjust stock level", description = "Adjust stock quantity for an inventory record")
    public ResponseEntity<Inventory> adjustStock(@PathVariable Long id, @RequestParam int adjustment) {
        Inventory inventory = inventoryService.adjustStock(id, adjustment);
        return ResponseEntity.ok(inventory);
    }
}
