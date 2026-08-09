package com.oem.evwarranty.domain.user;

import com.oem.evwarranty.common.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.oem.evwarranty.common.config.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.oem.evwarranty.domain.campaign.ServiceCampaignService campaignService;

    @MockBean
    private com.oem.evwarranty.domain.claim.WarrantyClaimService claimService;

    @MockBean
    private com.oem.evwarranty.domain.vehicle.VehicleService vehicleService;

    @MockBean
    private com.oem.evwarranty.domain.customer.CustomerService customerService;

    @MockBean
    private com.oem.evwarranty.domain.inventory.PartService partService;

    @MockBean
    private com.oem.evwarranty.domain.inventory.InventoryService inventoryService;

    @MockBean
    private com.oem.evwarranty.domain.analytics.ReportService reportService;

    @MockBean
    private com.oem.evwarranty.domain.audit.AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        when(userService.searchUsers(anyString(), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList(), org.springframework.data.domain.PageRequest.of(0, 10), 0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAccess_WithAdminRole_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminAccess_WithUserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAccess_Anonymous_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}
