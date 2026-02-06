package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ChatMessage entity for storing chat messages.
 * Supports GLOBAL (public) and WHISPER (private 1:1) message types.
 */
@Entity
@Table(name = "chat_messages",
    indexes = {
        @Index(name = "idx_chat_sender_id", columnList = "sender_id"),
        @Index(name = "idx_chat_receiver_id", columnList = "receiver_id"),
        @Index(name = "idx_chat_type_created", columnList = "message_type, created_at"),
        @Index(name = "idx_chat_whisper", columnList = "sender_id, receiver_id, created_at"),
        @Index(name = "idx_chat_unread", columnList = "receiver_id, read_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Player sender;

    @Column(name = "sender_username", nullable = false, length = 20)
    private String senderUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Player receiver;

    @Column(name = "receiver_username", length = 20)
    private String receiverUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Auto-populate denormalized usernames
        if (sender != null) {
            senderUsername = sender.getUsername();
        }
        if (receiver != null) {
            receiverUsername = receiver.getUsername();
        }
    }

    /**
     * Chat message type.
     */
    public enum MessageType {
        GLOBAL,   // Public chat visible to all (receiver is null)
        WHISPER   // Private message between two players
    }
}
