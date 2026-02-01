package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false, unique = true)
    private Long playerId;

    @Column(name = "current_run_floor", nullable = false)
    @Builder.Default
    private Integer currentRunFloor = 1;

    @Column(name = "current_run_hp", nullable = false)
    @Builder.Default
    private Integer currentRunHp = 100;

    @Column(name = "floor_1_cleared")
    @Builder.Default
    private Boolean floor1Cleared = false;

    @Column(name = "floor_2_cleared")
    @Builder.Default
    private Boolean floor2Cleared = false;

    @Column(name = "floor_3_cleared")
    @Builder.Default
    private Boolean floor3Cleared = false;

    @Column(name = "floor_4_cleared")
    @Builder.Default
    private Boolean floor4Cleared = false;

    @Column(name = "floor_5_cleared")
    @Builder.Default
    private Boolean floor5Cleared = false;

    @Column(name = "floor_6_cleared")
    @Builder.Default
    private Boolean floor6Cleared = false;

    @Column(name = "floor_7_cleared")
    @Builder.Default
    private Boolean floor7Cleared = false;

    @Column(name = "floor_8_cleared")
    @Builder.Default
    private Boolean floor8Cleared = false;

    @Column(name = "floor_9_cleared")
    @Builder.Default
    private Boolean floor9Cleared = false;

    @Column(name = "floor_10_cleared")
    @Builder.Default
    private Boolean floor10Cleared = false;

    @Column(name = "floor_11_cleared")
    @Builder.Default
    private Boolean floor11Cleared = false;

    @Column(name = "floor_12_cleared")
    @Builder.Default
    private Boolean floor12Cleared = false;

    @Column(name = "floor_13_cleared")
    @Builder.Default
    private Boolean floor13Cleared = false;

    @Column(name = "floor_14_cleared")
    @Builder.Default
    private Boolean floor14Cleared = false;

    @Column(name = "floor_15_cleared")
    @Builder.Default
    private Boolean floor15Cleared = false;

    @Column(name = "mammon_defeated")
    @Builder.Default
    private Boolean mammonDefeated = false;

    @Column(name = "eligor_defeated")
    @Builder.Default
    private Boolean eligorDefeated = false;

    @Column(name = "lucifuge_defeated")
    @Builder.Default
    private Boolean lucifugeDefeated = false;

    @Column(name = "total_runs", nullable = false)
    @Builder.Default
    private Integer totalRuns = 0;

    @Column(name = "total_victories", nullable = false)
    @Builder.Default
    private Integer totalVictories = 0;

    @Column(name = "total_defeats", nullable = false)
    @Builder.Default
    private Integer totalDefeats = 0;

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

    public boolean isFloorCleared(int floor) {
        return switch (floor) {
            case 1 -> Boolean.TRUE.equals(floor1Cleared);
            case 2 -> Boolean.TRUE.equals(floor2Cleared);
            case 3 -> Boolean.TRUE.equals(floor3Cleared);
            case 4 -> Boolean.TRUE.equals(floor4Cleared);
            case 5 -> Boolean.TRUE.equals(floor5Cleared);
            case 6 -> Boolean.TRUE.equals(floor6Cleared);
            case 7 -> Boolean.TRUE.equals(floor7Cleared);
            case 8 -> Boolean.TRUE.equals(floor8Cleared);
            case 9 -> Boolean.TRUE.equals(floor9Cleared);
            case 10 -> Boolean.TRUE.equals(floor10Cleared);
            case 11 -> Boolean.TRUE.equals(floor11Cleared);
            case 12 -> Boolean.TRUE.equals(floor12Cleared);
            case 13 -> Boolean.TRUE.equals(floor13Cleared);
            case 14 -> Boolean.TRUE.equals(floor14Cleared);
            case 15 -> Boolean.TRUE.equals(floor15Cleared);
            default -> false;
        };
    }

    public void markFloorCleared(int floor) {
        switch (floor) {
            case 1 -> floor1Cleared = true;
            case 2 -> floor2Cleared = true;
            case 3 -> floor3Cleared = true;
            case 4 -> floor4Cleared = true;
            case 5 -> floor5Cleared = true;
            case 6 -> floor6Cleared = true;
            case 7 -> floor7Cleared = true;
            case 8 -> floor8Cleared = true;
            case 9 -> floor9Cleared = true;
            case 10 -> floor10Cleared = true;
            case 11 -> floor11Cleared = true;
            case 12 -> floor12Cleared = true;
            case 13 -> floor13Cleared = true;
            case 14 -> floor14Cleared = true;
            case 15 -> floor15Cleared = true;
        }
    }

    public int getHighestClearedFloor() {
        for (int i = 15; i >= 1; i--) {
            if (isFloorCleared(i)) return i;
        }
        return 0;
    }
}
