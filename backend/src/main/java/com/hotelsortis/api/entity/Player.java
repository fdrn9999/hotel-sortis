package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User와 1:1 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false)
    @Builder.Default
    private Integer elo = 1000;

    @Column(name = "soul_stones", nullable = false)
    @Builder.Default
    private Integer soulStones = 0;

    @Column(name = "current_floor", nullable = false)
    @Builder.Default
    private Integer currentFloor = 1;

    @Column(name = "highest_floor_cleared", nullable = false)
    @Builder.Default
    private Integer highestFloorCleared = 0;

    @Column(name = "equipped_skill_ids", columnDefinition = "JSON")
    private String equippedSkillIds;

    @Column(name = "equipped_dice_skin_id")
    private Long equippedDiceSkinId;

    @Column(name = "equipped_avatar_id")
    private Long equippedAvatarId;

    @Column(name = "preferred_language", nullable = false, length = 2)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Language preferredLanguage = Language.en;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Language {
        ko, en, ja, zh
    }
}
