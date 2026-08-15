package com.oem.evwarranty.domain.chat;

import com.oem.evwarranty.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Messages & Chat History.
 * Base Path: /api/v1/messages
 */
@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "Messages & Support Chat", description = "Tin nhắn nội bộ, trò chuyện điều phối và kênh hỗ trợ trực tuyến")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/threads")
    @Operation(summary = "Danh sách hội thoại của người dùng", description = "Lấy danh sách các cuộc trò chuyện gần nhất, người liên hệ và số tin nhắn chưa đọc")
    public ResponseEntity<ApiResponse<List<ChatDTO.ThreadResponse>>> getThreads(Authentication authentication) {
        String username = authentication.getName();
        List<ChatDTO.ThreadResponse> threads = chatService.getUserThreads(username);
        return ResponseEntity.ok(ApiResponse.success(threads));
    }

    @GetMapping("/history")
    @Operation(summary = "Lịch sử tin nhắn của hội thoại", description = "Lấy toàn bộ tin nhắn theo ID hội thoại và tự động đánh dấu đã đọc")
    public ResponseEntity<ApiResponse<List<ChatDTO.MessageResponse>>> getHistory(
            @RequestParam Long conversationId,
            Authentication authentication) {

        String username = authentication.getName();
        List<ChatDTO.MessageResponse> history = chatService.getConversationHistory(conversationId, username);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PostMapping("/send")
    @Operation(summary = "Gửi tin nhắn qua REST API", description = "Gửi tin nhắn văn bản kèm tệp đính kèm và phát sóng qua WebSocket")
    public ResponseEntity<ApiResponse<ChatDTO.MessageResponse>> sendMessage(
            @Valid @RequestBody ChatDTO.SendMessageRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        ChatDTO.MessageResponse message = chatService.sendMessage(request, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tin nhắn đã gửi thành công", message));
    }
}
