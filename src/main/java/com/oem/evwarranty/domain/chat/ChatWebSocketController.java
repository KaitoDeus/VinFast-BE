package com.oem.evwarranty.domain.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket STOMP Controller for real-time bi-directional chat messaging.
 */
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage")
    public void handleIncomingMessage(@Payload ChatDTO.SocketMessagePayload payload, Principal principal) {
        if (principal == null || payload.getRecipientId() == null || payload.getContent() == null) {
            return;
        }

        ChatDTO.SendMessageRequest request = new ChatDTO.SendMessageRequest(
                payload.getConversationId(),
                payload.getRecipientId(),
                payload.getContent(),
                payload.getAttachmentUrls()
        );

        chatService.sendMessage(request, principal.getName());
    }
}
