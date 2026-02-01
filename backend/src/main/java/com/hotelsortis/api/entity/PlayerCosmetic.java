package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_cosmetics",
    uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "cosmetic_type", "cosmetic_id"}),
    indexes = {
        @Index(name = "idx_player_id", columnList = "player_id"),
        @Index(name = "idx_cosmetic_type", columnList = "cosmetic_type"),
        @Index(name = "idx_player_type", columnList = "player_id, cosmetic_type")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCosmetic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(name = "cosmetic_type", nullable = false)
    private CosmeticType cosmeticType;

    @Column(name = "cosmetic_id", nullable = false)
    private Long cosmeticId;

    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;

    @PrePersist
    protected void onCreate() {
        purchasedAt = LocalDateTime.now();
    }

    public enum CosmeticType {
        DICE_SKIN, AVATAR
    }
}
