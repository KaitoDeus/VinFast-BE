package com.oem.evwarranty.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderBySentAtAsc(Long conversationId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :convId AND m.recipient.id = :recipientId AND m.isRead = false")
    long countUnreadMessages(@Param("convId") Long convId, @Param("recipientId") Long recipientId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversation.id = :convId AND m.recipient.id = :recipientId")
    void markConversationAsRead(@Param("convId") Long convId, @Param("recipientId") Long recipientId);
}
