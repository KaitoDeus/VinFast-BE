package com.oem.evwarranty.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.participantOne.id = :userId OR c.participantTwo.id = :userId ORDER BY c.lastMessageTime DESC")
    List<Conversation> findUserConversations(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.participantOne.id = :userA AND c.participantTwo.id = :userB) OR " +
            "(c.participantOne.id = :userB AND c.participantTwo.id = :userA)")
    Optional<Conversation> findBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);
}
