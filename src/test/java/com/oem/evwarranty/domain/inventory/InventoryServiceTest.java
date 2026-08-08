package com.oem.evwarranty.domain.inventory;



import com.oem.evwarranty.domain.inventory.InventoryService;
import com.oem.evwarranty.domain.inventory.InventoryRepository;
import com.oem.evwarranty.domain.inventory.Inventory;
import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PartRepository partRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;
    private Part part;

    @BeforeEach
    void setUp() {
        part = new Part();
        part.setId(1L);
        part.setMinStockLevel(5);

        inventory = Inventory.builder()
                .id(1L)
                .part(part)
                .serviceCenter("SC-HANOI-01")
                .quantityOnHand(10)
                .quantityReserved(2)
                .build();
    }

    @Test
    void adjustStock_Valid_Success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        Inventory updated = inventoryService.adjustStock(1L, 5);

        assertEquals(15, updated.getQuantityOnHand());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void adjustStock_NegativeResult_ThrowsException() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.adjustStock(1L, -15);
        });
    }

    @Test
    void reserveStock_Insufficient_ThrowsException() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        // Available = 10 - 2 = 8

        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.reserveStock(1L, 10);
        });
    }

    @Test
    void createOrUpdateInventory_New_Success() {
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(inventoryRepository.findByPartIdAndServiceCenter(1L, "SC-HANOI-01")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        Inventory created = inventoryService.createOrUpdateInventory(1L, "SC-HANOI-01", 10);

        assertNotNull(created);
        verify(inventoryRepository).save(any(Inventory.class));
    }
}
