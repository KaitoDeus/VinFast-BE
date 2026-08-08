package com.oem.evwarranty.domain.campaign;



import com.oem.evwarranty.domain.campaign.ServiceCampaign;
import com.oem.evwarranty.domain.campaign.ServiceCampaignRepository;
import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.campaign.ServiceCampaignService;
import com.oem.evwarranty.domain.user.UserRepository;

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
public class ServiceCampaignServiceTest {

    @Mock
    private ServiceCampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ServiceCampaignService campaignService;

    private ServiceCampaign campaign;
    private User creator;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setUsername("admin");

        campaign = new ServiceCampaign();
        campaign.setId(1L);
        campaign.setTitle("Battery Recall");
        campaign.setStatus(ServiceCampaign.CampaignStatus.DRAFT);
    }

    @Test
    void createCampaign_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(campaignRepository.save(any(ServiceCampaign.class))).thenReturn(campaign);

        ServiceCampaign created = campaignService.createCampaign(campaign, 1L);

        assertNotNull(created);
        assertEquals(ServiceCampaign.CampaignStatus.DRAFT, created.getStatus());
        assertNotNull(created.getCampaignNumber());
        verify(campaignRepository).save(campaign);
    }

    @Test
    void activateCampaign_Success() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any(ServiceCampaign.class))).thenReturn(campaign);

        ServiceCampaign activated = campaignService.activateCampaign(1L);

        assertEquals(ServiceCampaign.CampaignStatus.ACTIVE, activated.getStatus());
    }

    @Test
    void incrementCompletedCount_Success() {
        campaign.setCompletedCount(5);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        campaignService.incrementCompletedCount(1L);

        assertEquals(6, campaign.getCompletedCount());
        verify(campaignRepository).save(campaign);
    }
}
