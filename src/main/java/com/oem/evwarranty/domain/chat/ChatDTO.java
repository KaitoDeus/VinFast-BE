package com.oem.evwarranty.domain.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ChatDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreadResponse {
        private Long id;
        private Long contactId;
        private String contactName;
        private String contactEmail;
        private String contactRole;
        private String avatarUrl;
        private String lastMessage;
        private String lastMessageTime;
        private long unreadCount;
        @Builder.Default
        private boolean online = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private Long id;
        private Long conversationId;
        private Long senderId;
        private String senderName;
        private String senderAvatar;
        private Long recipientId;
        private String content;
        private List<String> attachmentUrls;
        private String timestamp;
        private boolean isOwn;
        private boolean isRead;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageRequest {
        private Long conversationId;

        @NotNull(message = "Recipient ID is required")
        private Long recipientId;

        @NotBlank(message = "Message content cannot be blank")
        private String content;

        private List<String> attachmentUrls;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocketMessagePayload {
        private Long conversationId;
        private Long recipientId;
        private String content;
        private List<String> attachmentUrls;
    }
}
