package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.inventory.Inventory;


import com.oem.evwarranty.domain.analytics.ReportService;
import com.oem.evwarranty.domain.inventory.InventoryRepository;
import com.oem.evwarranty.domain.campaign.ServiceCampaignRepository;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.claim.WarrantyClaimRepository;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;
import com.oem.evwarranty.domain.inventory.PartRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private WarrantyClaimRepository claimRepository;
    @Mock
    private ServiceCampaignRepository campaignRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PartRepository partRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void testGetDashboardStats() {
        // Mock data
        when(vehicleRepository.count()).thenReturn(100L);
        when(vehicleRepository.countByStatus(Vehicle.VehicleStatus.ACTIVE)).thenReturn(80L);
        when(vehicleRepository.findVehiclesUnderWarranty()).thenReturn(Collections.emptyList());
        when(claimRepository.count()).thenReturn(50L);
        when(campaignRepository.count()).thenReturn(5L);
        when(customerRepository.count()).thenReturn(90L);
        when(partRepository.count()).thenReturn(200L);
        when(inventoryRepository.findLowStockItems()).thenReturn(Collections.emptyList());

        Map<String, Object> stats = reportService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(100L, stats.get("totalVehicles"));
        assertEquals(50L, stats.get("totalClaims"));
        assertEquals(90L, stats.get("totalCustomers"));
    }
}
