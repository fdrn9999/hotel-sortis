package com.hotelsortis.api.repository;

import com.hotelsortis.api.entity.ChatMessage;
import com.hotelsortis.api.entity.ChatMessage.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ChatMessage entity.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Get recent messages by type (paginated, newest first).
     * Usage: Display global chat room.
     */
    Page<ChatMessage> findByMessageTypeOrderByCreatedAtDesc(MessageType messageType, Pageable pageable);

    /**
     * Get whisper conversation between two players (paginated, newest first).
     * Usage: Display private chat history.
     */
    @Query("SELECT c FROM ChatMessage c " +
           "WHERE c.messageType = 'WHISPER' " +
           "AND ((c.sender.id = :playerId1 AND c.receiver.id = :playerId2) " +
           "OR (c.sender.id = :playerId2 AND c.receiver.id = :playerId1)) " +
           "ORDER BY c.createdAt DESC")
    Page<ChatMessage> findWhisperConversation(
        @Param("playerId1") Long playerId1,
        @Param("playerId2") Long playerId2,
        Pageable pageable
    );

    /**
     * Get unread whispers for a player.
     * Usage: Notification system.
     */
    List<ChatMessage> findByReceiverIdAndMessageTypeAndReadAtIsNullOrderByCreatedAtDesc(
        Long receiverId,
        MessageType messageType
    );

    /**
     * Count unread whispers for a player.
     * Usage: Display unread message badge.
     */
    int countByReceiverIdAndMessageTypeAndReadAtIsNull(Long receiverId, MessageType messageType);

    /**
     * Mark message as read.
     * Usage: Update read status when player views message.
     */
    @Modifying
    @Query("UPDATE ChatMessage c SET c.readAt = :readAt WHERE c.id = :messageId AND c.readAt IS NULL")
    int updateReadAt(@Param("messageId") Long messageId, @Param("readAt") LocalDateTime readAt);

    /**
     * Mark all messages in conversation as read.
     * Usage: Mark all as read when opening chat with a player.
     */
    @Modifying
    @Query("UPDATE ChatMessage c SET c.readAt = :readAt " +
           "WHERE c.receiver.id = :receiverId AND c.sender.id = :senderId AND c.readAt IS NULL")
    int markConversationAsRead(
        @Param("receiverId") Long receiverId,
        @Param("senderId") Long senderId,
        @Param("readAt") LocalDateTime readAt
    );

    /**
     * Delete old global messages (cleanup job).
     * Usage: Scheduled task to prevent table bloat.
     */
    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.messageType = 'GLOBAL' AND c.createdAt < :cutoffDate")
    int deleteOldGlobalMessages(@Param("cutoffDate") LocalDateTime cutoffDate);
}
