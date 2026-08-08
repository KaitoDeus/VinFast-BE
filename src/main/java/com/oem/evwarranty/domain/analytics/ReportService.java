package com.oem.evwarranty.domain.analytics;

import com.oem.evwarranty.domain.customer.Customer;
import com.oem.evwarranty.domain.inventory.Inventory;


import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import com.oem.evwarranty.domain.vehicle.Vehicle;
import com.oem.evwarranty.domain.claim.WarrantyClaim;
import com.oem.evwarranty.domain.customer.CustomerRepository;
import com.oem.evwarranty.domain.inventory.InventoryRepository;
import com.oem.evwarranty.domain.inventory.PartRepository;
import com.oem.evwarranty.domain.campaign.ServiceCampaignRepository;
import com.oem.evwarranty.domain.vehicle.VehicleRepository;
import com.oem.evwarranty.domain.claim.WarrantyClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for Dashboard and Reports.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final VehicleRepository vehicleRepository;
    private final WarrantyClaimRepository claimRepository;
    private final ServiceCampaignRepository campaignRepository;
    private final CustomerRepository customerRepository;
    private final PartRepository partRepository;
    private final InventoryRepository inventoryRepository;

    public ReportService(VehicleRepository vehicleRepository,
            WarrantyClaimRepository claimRepository,
            ServiceCampaignRepository campaignRepository,
            CustomerRepository customerRepository,
            PartRepository partRepository,
            InventoryRepository inventoryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.claimRepository = claimRepository;
        this.campaignRepository = campaignRepository;
        this.customerRepository = customerRepository;
        this.partRepository = partRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalVehicles", vehicleRepository.count());
        stats.put("activeVehicles", vehicleRepository.countByStatus(Vehicle.VehicleStatus.ACTIVE));
        stats.put("vehiclesUnderWarranty", vehicleRepository.findVehiclesUnderWarranty().size());

        stats.put("totalClaims", claimRepository.count());
        stats.put("pendingClaims", claimRepository.countByStatus(WarrantyClaim.ClaimStatus.SUBMITTED) +
                claimRepository.countByStatus(WarrantyClaim.ClaimStatus.UNDER_REVIEW));
        stats.put("approvedClaims", claimRepository.countByStatus(WarrantyClaim.ClaimStatus.APPROVED));
        stats.put("completedClaims", claimRepository.countByStatus(WarrantyClaim.ClaimStatus.COMPLETED));

        stats.put("totalCampaigns", campaignRepository.count());
        stats.put("activeCampaigns", campaignRepository.countByStatus(ServiceCampaign.CampaignStatus.ACTIVE));
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalParts", partRepository.count());
        stats.put("lowStockItems", inventoryRepository.findLowStockItems().size());

        return stats;
    }

    /**
     * Get Service Center specific statistics
     */
    public Map<String, Object> getServiceCenterStats(String serviceCenter) {
        Map<String, Object> stats = new HashMap<>();
        List<WarrantyClaim> scClaims = claimRepository.findByServiceCenter(serviceCenter);

        Map<String, Long> claimStats = new HashMap<>();
        for (WarrantyClaim.ClaimStatus status : WarrantyClaim.ClaimStatus.values()) {
            claimStats.put(status.name(), scClaims.stream().filter(c -> c.getStatus() == status).count());
        }
        stats.put("claimStats", claimStats);

        stats.put("pendingClaims", claimStats.getOrDefault(WarrantyClaim.ClaimStatus.SUBMITTED.name(), 0L) +
                claimStats.getOrDefault(WarrantyClaim.ClaimStatus.UNDER_REVIEW.name(), 0L));
        stats.put("completedClaims", claimStats.getOrDefault(WarrantyClaim.ClaimStatus.COMPLETED.name(), 0L));
        stats.put("inProgressClaims", claimStats.getOrDefault(WarrantyClaim.ClaimStatus.IN_PROGRESS.name(), 0L));

        stats.put("vehicleStats", getVehicleStatsByStatus());
        stats.put("campaignStats", getCampaignStatsByStatus());
        stats.put("totalVehicles", vehicleRepository.count());
        stats.put("activeCampaigns", campaignRepository.countByStatus(ServiceCampaign.CampaignStatus.ACTIVE));
        stats.put("lowStockItems", inventoryRepository.findLowStockItemsByServiceCenter(serviceCenter).size());

        return stats;
    }

    /**
     * Get claim statistics by status
     */
    public Map<String, Long> getClaimStatsByStatus() {
        Map<String, Long> stats = new HashMap<>();
        for (WarrantyClaim.ClaimStatus status : WarrantyClaim.ClaimStatus.values()) {
            stats.put(status.name(), claimRepository.countByStatus(status));
        }
        return stats;
    }

    /**
     * Get campaign statistics by status
     */
    public Map<String, Long> getCampaignStatsByStatus() {
        Map<String, Long> stats = new HashMap<>();
        for (ServiceCampaign.CampaignStatus status : ServiceCampaign.CampaignStatus.values()) {
            stats.put(status.name(), campaignRepository.countByStatus(status));
        }
        return stats;
    }

    /**
     * Get vehicle statistics by status
     */
    public Map<String, Long> getVehicleStatsByStatus() {
        Map<String, Long> stats = new HashMap<>();
        for (Vehicle.VehicleStatus status : Vehicle.VehicleStatus.values()) {
            stats.put(status.name(), vehicleRepository.countByStatus(status));
        }
        return stats;
    }
}



