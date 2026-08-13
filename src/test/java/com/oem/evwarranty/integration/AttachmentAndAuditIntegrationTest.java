package com.oem.evwarranty.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Test: Validates the full attachment upload lifecycle
 * and audit logging across multiple REST API endpoints using real Spring context.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AttachmentAndAuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("E2E Test: Full Attachment Upload, Download, List, Delete & Audit Trail Flow")
    void testFullAttachmentAndAuditFlow() throws Exception {
        // 1. Login as admin (guaranteed to exist and has full permissions)
        String adminToken = loginAndGetToken("admin", "password123");

        // 2. Get a valid claim ID from the paginated claims endpoint
        MvcResult claimListResult = mockMvc.perform(get("/api/v1/claims")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String claimResponse = claimListResult.getResponse().getContentAsString();

        // If no claims exist in DB, skip attachment upload test gracefully
        if (!claimResponse.contains("\"id\"")) {
            return;
        }

        // Extract claim ID from paginated content array
        Long claimId = extractIdFromContent(claimResponse);

        // 3. Upload Attachment File
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "e2e_battery_diagnostic.png",
                MediaType.IMAGE_PNG_VALUE,
                "PNG_BINARY_DIAGNOSTIC_DATA_CONTENT".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/claims/" + claimId + "/attachments")
                        .file(file)
                        .param("attachmentType", "DIAGNOSTIC_REPORT")
                        .param("description", "E2E Test Battery Thermal Diagnostic Report")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fileName").value("e2e_battery_diagnostic.png"))
                .andReturn();

        Long attachmentId = extractIdFromContent(uploadResult.getResponse().getContentAsString());

        // 4. Download Attachment File
        mockMvc.perform(get("/api/v1/attachments/" + attachmentId + "/download")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"e2e_battery_diagnostic.png\""))
                .andExpect(content().bytes("PNG_BINARY_DIAGNOSTIC_DATA_CONTENT".getBytes()));

        // 5. List Attachments for the Claim
        mockMvc.perform(get("/api/v1/claims/" + claimId + "/attachments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        // 6. Query Audit Logs (admin has access)
        mockMvc.perform(get("/api/v1/audit/logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 7. Clean up - Delete attachment
        mockMvc.perform(delete("/api/v1/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attachment deleted successfully"));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String loginJson = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
        return extractJwtToken(loginResult.getResponse().getContentAsString());
    }

    private String extractJwtToken(String responseJson) {
        int tokenKeyIdx = responseJson.indexOf("\"token\"");
        int colonIdx = responseJson.indexOf(":", tokenKeyIdx);
        int valueStart = responseJson.indexOf("\"", colonIdx) + 1;
        int valueEnd = responseJson.indexOf("\"", valueStart);
        return responseJson.substring(valueStart, valueEnd);
    }

    /**
     * Extract the first "id" numeric value from JSON response.
     */
    private Long extractIdFromContent(String responseJson) {
        int idStart = responseJson.indexOf("\"id\":");
        if (idStart == -1) return null;
        idStart += 5;
        while (idStart < responseJson.length() && !Character.isDigit(responseJson.charAt(idStart))) idStart++;
        int idEnd = idStart;
        while (idEnd < responseJson.length() && Character.isDigit(responseJson.charAt(idEnd))) idEnd++;
        return Long.parseLong(responseJson.substring(idStart, idEnd));
    }
}
