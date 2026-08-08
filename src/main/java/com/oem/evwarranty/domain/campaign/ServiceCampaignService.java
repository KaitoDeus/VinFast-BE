package com.oem.evwarranty.domain.campaign;

import com.oem.evwarranty.domain.vehicle.Vehicle;

import com.oem.evwarranty.domain.vehicle.VehicleRepository;

import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.campaign.ServiceCampaignRepository;
import com.oem.evwarranty.domain.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for Service Campaign (Recall) management operations.
 */
@Service
@Transactional
public class ServiceCampaignService {

    private final ServiceCampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public ServiceCampaignService(ServiceCampaignRepository campaignRepository, UserRepository userRepository) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
    }

    public List<ServiceCampaign> findAll() {
        return campaignRepository.findAll();
    }

    public Optional<ServiceCampaign> findById(@NonNull Long id) {
        return campaignRepository.findById(id);
    }

    public Optional<ServiceCampaign> findByCampaignNumber(String campaignNumber) {
        return campaignRepository.findByCampaignNumber(campaignNumber);
    }

    public Page<ServiceCampaign> findAll(@NonNull Pageable pageable) {
        return campaignRepository.findAll(pageable);
    }

    public Page<ServiceCampaign> searchCampaigns(String search, @NonNull Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return campaignRepository.findAll(pageable);
        }
        return campaignRepository.searchCampaigns(search, pageable);
    }

    public List<ServiceCampaign> findActiveCampaigns() {
        return campaignRepository.findActiveCampaigns();
    }

    public List<ServiceCampaign> findByStatus(ServiceCampaign.CampaignStatus status) {
        return campaignRepository.findByStatus(status);
    }

    public ServiceCampaign createCampaign(ServiceCampaign campaign, Long createdById) {
        if (createdById != null) {
            User creator = userRepository.findById(createdById)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            campaign.setCreatedBy(creator);
        }

        campaign.setCampaignNumber("SC" + System.currentTimeMillis());
        campaign.setStatus(ServiceCampaign.CampaignStatus.DRAFT);

        return campaignRepository.save(campaign);
    }

    public ServiceCampaign updateCampaign(@NonNull Long id, ServiceCampaign updatedCampaign) {
        return campaignRepository.findById(id)
                .map(campaign -> {
                    campaign.setTitle(updatedCampaign.getTitle());
                    campaign.setDescription(updatedCampaign.getDescription());
                    campaign.setCampaignType(updatedCampaign.getCampaignType());
                    campaign.setSeverityLevel(updatedCampaign.getSeverityLevel());
                    campaign.setAffectedModels(updatedCampaign.getAffectedModels());
                    campaign.setAffectedVins(updatedCampaign.getAffectedVins());
                    campaign.setAffectedParts(updatedCampaign.getAffectedParts());
                    campaign.setRemedyDescription(updatedCampaign.getRemedyDescription());
                    campaign.setEstimatedRepairTime(updatedCampaign.getEstimatedRepairTime());
                    campaign.setStartDate(updatedCampaign.getStartDate());
                    campaign.setEndDate(updatedCampaign.getEndDate());
                    campaign.setTotalAffected(updatedCampaign.getTotalAffected());
                    return campaignRepository.save(campaign);
                })
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
    }

    public ServiceCampaign activateCampaign(@NonNull Long id) {
        return campaignRepository.findById(id)
                .map(campaign -> {
                    campaign.setStatus(ServiceCampaign.CampaignStatus.ACTIVE);
                    return campaignRepository.save(campaign);
                })
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
    }

    public ServiceCampaign completeCampaign(@NonNull Long id) {
        return campaignRepository.findById(id)
                .map(campaign -> {
                    campaign.setStatus(ServiceCampaign.CampaignStatus.COMPLETED);
                    return campaignRepository.save(campaign);
                })
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
    }

    public void incrementCompletedCount(@NonNull Long id) {
        campaignRepository.findById(id).ifPresent(campaign -> {
            campaign.setCompletedCount(campaign.getCompletedCount() + 1);
            campaignRepository.save(campaign);
        });
    }

    public void deleteCampaign(@NonNull Long id) {
        campaignRepository.deleteById(id);
    }

    public long count() {
        return campaignRepository.count();
    }

    public long countByStatus(ServiceCampaign.CampaignStatus status) {
        return campaignRepository.countByStatus(status);
    }
}



