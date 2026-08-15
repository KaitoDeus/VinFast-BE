package com.oem.evwarranty.domain.chat;

import com.oem.evwarranty.domain.user.User;
import com.oem.evwarranty.domain.user.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public ChatService(ConversationRepository conversationRepository,
                       ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<ChatDTO.ThreadResponse> getUserThreads(String username) {
        User currentUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        List<Conversation> conversations = conversationRepository.findUserConversations(currentUser.getId());
        return conversations.stream()
                .map(c -> {
                    User contact = c.getParticipantOne().getId().equals(currentUser.getId())
                            ? c.getParticipantTwo() : c.getParticipantOne();

                    long unread = chatMessageRepository.countUnreadMessages(c.getId(), currentUser.getId());
                    String timeStr = c.getLastMessageTime() != null ? c.getLastMessageTime().format(TIME_FORMATTER) : "Just now";
                    String roleStr = contact.getRoles() != null && !contact.getRoles().isEmpty()
                            ? contact.getRoles().iterator().next().getName() : "CLIENT";

                    return ChatDTO.ThreadResponse.builder()
                            .id(c.getId())
                            .contactId(contact.getId())
                            .contactName(contact.getFullName() != null ? contact.getFullName() : contact.getUsername())
                            .contactEmail(contact.getEmail())
                            .contactRole(roleStr)
                            .avatarUrl(contact.getAvatarUrl() != null ? contact.getAvatarUrl() : "/team/avatar-1.png")
                            .lastMessage(c.getLastMessage() != null ? c.getLastMessage() : "Started conversation")
                            .lastMessageTime(timeStr)
                            .unreadCount(unread)
                            .online(true)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<ChatDTO.MessageResponse> getConversationHistory(Long conversationId, String username) {
        User currentUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        chatMessageRepository.markConversationAsRead(conversationId, currentUser.getId());

        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
        return messages.stream()
                .map(m -> {
                    boolean isOwn = m.getSender().getId().equals(currentUser.getId());
                    String timeStr = m.getSentAt() != null ? m.getSentAt().format(TIME_FORMATTER) : "Just now";
                    List<String> attachments = (m.getAttachmentUrls() != null && !m.getAttachmentUrls().isBlank())
                            ? Arrays.asList(m.getAttachmentUrls().split(",")) : List.of();

                    return ChatDTO.MessageResponse.builder()
                            .id(m.getId())
                            .conversationId(m.getConversation().getId())
                            .senderId(m.getSender().getId())
                            .senderName(m.getSender().getFullName())
                            .senderAvatar(m.getSender().getAvatarUrl())
                            .recipientId(m.getRecipient().getId())
                            .content(m.getContent())
                            .attachmentUrls(attachments)
                            .timestamp(timeStr)
                            .isOwn(isOwn)
                            .isRead(m.getIsRead())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public ChatDTO.MessageResponse sendMessage(ChatDTO.SendMessageRequest request, String senderUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .or(() -> userRepository.findByEmail(senderUsername))
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderUsername));

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found with ID: " + request.getRecipientId()));

        Conversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + request.getConversationId()));
        } else {
            conversation = conversationRepository.findBetweenUsers(sender.getId(), recipient.getId())
                    .orElseGet(() -> conversationRepository.save(Conversation.builder()
                            .participantOne(sender)
                            .participantTwo(recipient)
                            .lastMessage(request.getContent())
                            .lastMessageTime(LocalDateTime.now())
                            .build()));
        }

        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(conversation);

        String attachmentUrlsStr = (request.getAttachmentUrls() != null && !request.getAttachmentUrls().isEmpty())
                ? String.join(",", request.getAttachmentUrls()) : null;

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .attachmentUrls(attachmentUrlsStr)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        ChatDTO.MessageResponse response = ChatDTO.MessageResponse.builder()
                .id(saved.getId())
                .conversationId(conversation.getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .senderAvatar(sender.getAvatarUrl())
                .recipientId(recipient.getId())
                .content(saved.getContent())
                .attachmentUrls(request.getAttachmentUrls() != null ? request.getAttachmentUrls() : List.of())
                .timestamp(saved.getSentAt().format(TIME_FORMATTER))
                .isOwn(true)
                .isRead(false)
                .build();

        // Push to WebSocket destination
        try {
            messagingTemplate.convertAndSend("/topic/messages." + conversation.getId(), response);
            messagingTemplate.convertAndSendToUser(recipient.getUsername(), "/queue/messages", response);
        } catch (Exception ignored) {}

        return response;
    }
}
