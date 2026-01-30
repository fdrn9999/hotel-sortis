package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "battles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "enemy_id")
    private Long enemyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "battle_type", nullable = false)
    private BattleType battleType;

    @Column
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ONGOING;

    @Column(name = "player_hp", nullable = false)
    @Builder.Default
    private Integer playerHp = 100;

    @Column(name = "enemy_hp", nullable = false)
    @Builder.Default
    private Integer enemyHp = 100;

    @Column(name = "turn_count", nullable = false)
    @Builder.Default
    private Integer turnCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_turn", nullable = false)
    @Builder.Default
    private TurnActor currentTurn = TurnActor.PLAYER;

    @Column(name = "player_equipped_skills", columnDefinition = "JSON")
    private String playerEquippedSkills;

    @Column(name = "enemy_equipped_skills", columnDefinition = "JSON")
    private String enemyEquippedSkills;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }

    public enum BattleType {
        PVE, PVP
    }

    public enum Status {
        ONGOING, VICTORY, DEFEAT, DRAW
    }

    public enum TurnActor {
        PLAYER, ENEMY
    }
}
