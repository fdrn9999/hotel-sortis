package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * BlockList entity for managing player blocking.
 * Unidirectional: blocker blocks blocked (blocked player may not know).
 */
@Entity
@Table(name = "block_list",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_blocker_blocked",
        columnNames = {"blocker_id", "blocked_id"}
    ),
    indexes = {
        @Index(name = "idx_block_blocker_id", columnList = "blocker_id"),
        @Index(name = "idx_block_blocked_id", columnList = "blocked_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private Player blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private Player blocked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
