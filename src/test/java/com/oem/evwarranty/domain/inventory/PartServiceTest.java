package com.oem.evwarranty.domain.inventory;



import com.oem.evwarranty.domain.inventory.Part;
import com.oem.evwarranty.domain.inventory.PartRepository;
import com.oem.evwarranty.domain.inventory.PartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PartServiceTest {

    @Mock
    private PartRepository partRepository;

    @InjectMocks
    private PartService partService;

    private Part part;

    @BeforeEach
    void setUp() {
        part = new Part();
        part.setId(1L);
        part.setPartNumber("BAT-001");
        part.setName("Battery Pack");
        part.setPrice(new BigDecimal("5000.00"));
        part.setIsActive(true);
    }

    @Test
    void createPart_NewNumber_Success() {
        when(partRepository.existsByPartNumber(part.getPartNumber())).thenReturn(false);
        when(partRepository.save(any(Part.class))).thenReturn(part);

        Part created = partService.createPart(part);

        assertNotNull(created);
        assertEquals(part.getPartNumber(), created.getPartNumber());
        verify(partRepository).save(part);
    }

    @Test
    void createPart_ExistingNumber_ThrowsException() {
        when(partRepository.existsByPartNumber(part.getPartNumber())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            partService.createPart(part);
        });
    }

    @Test
    void updatePart_NotFound_ThrowsException() {
        when(partRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            partService.updatePart(1L, part);
        });
    }

    @Test
    void togglePartStatus_Success() {
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        partService.togglePartStatus(1L);

        assertFalse(part.getIsActive());
        verify(partRepository).save(part);
    }
}
