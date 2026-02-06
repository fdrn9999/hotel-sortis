package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Friend entity for managing player friendships.
 * Bidirectional: when ACCEPTED, two rows exist (A→B, B→A).
 */
@Entity
@Table(name = "friends",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_player_friend",
        columnNames = {"player_id", "friend_id"}
    ),
    indexes = {
        @Index(name = "idx_friend_player_status", columnList = "player_id, status"),
        @Index(name = "idx_friend_friend_status", columnList = "friend_id, status"),
        @Index(name = "idx_friend_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private Player friend;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FriendStatus status = FriendStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Friend relationship status.
     */
    public enum FriendStatus {
        PENDING,   // Request sent, awaiting acceptance
        ACCEPTED   // Friendship confirmed (bidirectional)
    }
}
