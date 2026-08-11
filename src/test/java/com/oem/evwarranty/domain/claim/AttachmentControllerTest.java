package com.oem.evwarranty.domain.claim;

import com.oem.evwarranty.common.config.JwtTokenProvider;
import com.oem.evwarranty.domain.user.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttachmentController.class)
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttachmentService attachmentService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private WarrantyClaim sampleClaim;
    private ClaimAttachment sampleAttachment;

    @BeforeEach
    void setUp() {
        sampleClaim = new WarrantyClaim();
        sampleClaim.setId(1L);
        sampleClaim.setClaimNumber("CLM2024001");

        sampleAttachment = ClaimAttachment.builder()
                .id(10L)
                .warrantyClaim(sampleClaim)
                .fileName("diagnostic_photo.jpg")
                .filePath("uuid_diagnostic_photo.jpg")
                .fileType("image/jpeg")
                .fileSize(1024L)
                .attachmentType(ClaimAttachment.AttachmentType.PHOTO)
                .description("Front battery diagnostic photo")
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "scstaff", roles = {"SC_STAFF"})
    void testUploadAttachmentSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagnostic_photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image content".getBytes()
        );

        given(attachmentService.uploadAttachment(eq(1L), any(), any(), any(), anyString()))
                .willReturn(sampleAttachment);

        mockMvc.perform(multipart("/api/v1/claims/1/attachments")
                        .file(file)
                        .param("attachmentType", "PHOTO")
                        .param("description", "Front battery diagnostic photo")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.fileName").value("diagnostic_photo.jpg"))
                .andExpect(jsonPath("$.attachmentType").value("PHOTO"));
    }

    @Test
    @WithMockUser(username = "scstaff", roles = {"SC_STAFF"})
    void testDownloadAttachmentFileSuccess() throws Exception {
        byte[] fileBytes = "fake binary content".getBytes();
        Resource resource = new ByteArrayResource(fileBytes);

        given(attachmentService.getAttachmentById(10L)).willReturn(sampleAttachment);
        given(attachmentService.downloadAttachmentFile(10L)).willReturn(resource);

        mockMvc.perform(get("/api/v1/attachments/10/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"diagnostic_photo.jpg\""))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().bytes(fileBytes));
    }

    @Test
    @WithMockUser(username = "scstaff", roles = {"SC_STAFF"})
    void testGetAttachmentsByClaimSuccess() throws Exception {
        given(attachmentService.getAttachmentsByClaimId(1L)).willReturn(List.of(sampleAttachment));

        mockMvc.perform(get("/api/v1/claims/1/attachments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].fileName").value("diagnostic_photo.jpg"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteAttachmentSuccess() throws Exception {
        doNothing().when(attachmentService).deleteAttachment(10L);

        mockMvc.perform(delete("/api/v1/attachments/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attachment deleted successfully"));
    }
}
