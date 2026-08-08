package com.oem.evwarranty.domain.inventory;

import com.oem.evwarranty.domain.user.User;

import com.oem.evwarranty.domain.user.UserRepository;

import com.oem.evwarranty.domain.inventory.Inventory;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.InventoryRepository;
import com.oem.evwarranty.domain.inventory.PartRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Inventory management operations.
 */
@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final PartRepository partRepository;

    public InventoryService(InventoryRepository inventoryRepository, PartRepository partRepository) {
        this.inventoryRepository = inventoryRepository;
        this.partRepository = partRepository;
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> findById(@NonNull Long id) {
        return inventoryRepository.findById(id);
    }

    public List<Inventory> findByServiceCenter(String serviceCenter) {
        return inventoryRepository.findByServiceCenter(serviceCenter);
    }

    public Optional<Inventory> findByPartAndServiceCenter(@NonNull Long partId, String serviceCenter) {
        return inventoryRepository.findByPartIdAndServiceCenter(partId, serviceCenter);
    }

    public List<Inventory> findLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<Inventory> findLowStockItemsByServiceCenter(String serviceCenter) {
        return inventoryRepository.findLowStockItemsByServiceCenter(serviceCenter);
    }

    public Inventory createOrUpdateInventory(@NonNull Long partId, String serviceCenter, int quantity) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new IllegalArgumentException("Part not found"));

        Optional<Inventory> existingInventory = inventoryRepository.findByPartIdAndServiceCenter(partId, serviceCenter);

        if (existingInventory.isPresent()) {
            Inventory inventory = existingInventory.get();
            inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
            inventory.setLastRestockDate(LocalDateTime.now());
            return inventoryRepository.save(inventory);
        } else {
            Inventory inventory = Inventory.builder()
                    .part(part)
                    .serviceCenter(serviceCenter)
                    .quantityOnHand(quantity)
                    .reorderPoint(part.getMinStockLevel())
                    .lastRestockDate(LocalDateTime.now())
                    .build();
            return inventoryRepository.save(inventory);
        }
    }

    public Inventory adjustStock(@NonNull Long id, int adjustment) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    int newQuantity = inventory.getQuantityOnHand() + adjustment;
                    if (newQuantity < 0) {
                        throw new IllegalArgumentException("Cannot have negative stock");
                    }
                    inventory.setQuantityOnHand(newQuantity);
                    return inventoryRepository.save(inventory);
                })
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
    }

    public Inventory reserveStock(@NonNull Long id, int quantity) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    if (inventory.getAvailableQuantity() < quantity) {
                        throw new IllegalArgumentException("Insufficient available stock");
                    }
                    inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
                    return inventoryRepository.save(inventory);
                })
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
    }

    public Inventory releaseReservation(@NonNull Long id, int quantity) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    int newReserved = inventory.getQuantityReserved() - quantity;
                    inventory.setQuantityReserved(Math.max(0, newReserved));
                    return inventoryRepository.save(inventory);
                })
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
    }

    public Inventory consumeStock(@NonNull Long id, int quantity) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
                    inventory.setQuantityReserved(Math.max(0, inventory.getQuantityReserved() - quantity));
                    return inventoryRepository.save(inventory);
                })
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
    }

    public void deleteInventory(@NonNull Long id) {
        inventoryRepository.deleteById(id);
    }
}



