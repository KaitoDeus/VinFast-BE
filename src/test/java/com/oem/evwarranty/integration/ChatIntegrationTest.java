package com.oem.evwarranty.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("GET /api/v1/messages/threads - User conversation threads")
    void testGetThreads() throws Exception {
        mockMvc.perform(get("/api/v1/messages/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("POST /api/v1/messages/send - Send chat message between users")
    void testSendMessage() throws Exception {
        String messageJson = """
                {
                    "recipientId": 2,
                    "content": "Xin chào, xe của bạn đã sẵn sàng tại trạm sạc!",
                    "attachmentUrls": ["https://vinfast.vn/car.png"]
                }
                """;

        mockMvc.perform(post("/api/v1/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(messageJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Xin chào, xe của bạn đã sẵn sàng tại trạm sạc!"))
                .andExpect(jsonPath("$.data.isOwn").value(true));
    }
}
