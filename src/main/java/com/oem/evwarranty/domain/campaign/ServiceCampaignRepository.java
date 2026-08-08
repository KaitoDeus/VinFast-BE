package com.oem.evwarranty.domain.campaign;


import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository for ServiceCampaign entity operations.
 */
@Repository
public interface ServiceCampaignRepository extends JpaRepository<ServiceCampaign, Long> {

    Optional<ServiceCampaign> findByCampaignNumber(String campaignNumber);

    List<ServiceCampaign> findByStatus(ServiceCampaign.CampaignStatus status);

    List<ServiceCampaign> findByCampaignType(ServiceCampaign.CampaignType campaignType);

    @Query("SELECT sc FROM ServiceCampaign sc WHERE sc.status = 'ACTIVE'")
    List<ServiceCampaign> findActiveCampaigns();

    @Query("SELECT sc FROM ServiceCampaign sc WHERE " +
            "LOWER(sc.campaignNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sc.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ServiceCampaign> searchCampaigns(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(sc) FROM ServiceCampaign sc WHERE sc.status = :status")
    Long countByStatus(@Param("status") ServiceCampaign.CampaignStatus status);
}


