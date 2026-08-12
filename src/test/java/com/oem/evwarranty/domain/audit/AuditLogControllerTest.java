package com.oem.evwarranty.domain.audit;

import com.oem.evwarranty.common.config.JwtTokenProvider;
import com.oem.evwarranty.domain.user.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.oem.evwarranty.common.config.SecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(AuditLogRestController.class)
@Import(SecurityConfig.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = AuditLog.builder()
                .id(1L)
                .username("admin")
                .action("CREATE")
                .resourceType("WARRANTY_CLAIM")
                .resourceId(100L)
                .details("Created claim CLM2024001")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllAuditLogsSuccess() throws Exception {
        given(auditLogService.findAll(any()))
                .willReturn(new PageImpl<>(List.of(sampleLog), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/audit/logs")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("admin"))
                .andExpect(jsonPath("$.content[0].action").value("CREATE"));
    }

    @Test
    @WithMockUser(username = "scstaff", roles = {"SC_STAFF"})
    void testGetResourceLogsSuccess() throws Exception {
        given(auditLogService.getLogsForResource("WARRANTY_CLAIM", 100L))
                .willReturn(List.of(sampleLog));

        mockMvc.perform(get("/api/v1/audit/resource/WARRANTY_CLAIM/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].resourceType").value("WARRANTY_CLAIM"));
    }

    @Test
    @WithMockUser(username = "evmstaff", roles = {"EVM_STAFF"})
    void testGetUserLogsSuccess() throws Exception {
        given(auditLogService.getLogsForUser("admin"))
                .willReturn(List.of(sampleLog));

        mockMvc.perform(get("/api/v1/audit/user/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"));
    }

    @Test
    @WithMockUser(username = "scstaff", roles = {"SC_STAFF"})
    void testGetAllAuditLogsForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs"))
                .andExpect(status().isForbidden());
    }
}
