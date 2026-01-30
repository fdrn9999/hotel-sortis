package com.hotelsortis.api.dto;

import com.hotelsortis.api.entity.Battle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class BattleDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartRequest {
        private Long playerId;
        private String battleType; // PVE or PVP
        private Integer floor;
        private List<Long> equippedSkills; // max 4
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartResponse {
        private Long battleId;
        private Long playerId;
        private Long enemyId;
        private String currentTurn;
        private Integer playerHp;
        private Integer enemyHp;
        private Integer turnCount;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RollRequest {
        private Long playerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RollResponse {
        private int[] dice;
        private String hash;
        private HandResult hand;
        private Integer damage;
        private Integer playerHp;
        private Integer enemyHp;
        private String currentTurn;
        private String status;
        private EnemyTurnResult enemyTurn; // AI의 턴 결과 (PvE)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HandResult {
        private String rank;
        private String rankKR;
        private Integer power;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnemyTurnResult {
        private int[] dice;
        private HandResult hand;
        private Integer damage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BattleStatus {
        private Long battleId;
        private Integer playerHp;
        private Integer enemyHp;
        private Integer turnCount;
        private String currentTurn;
        private String status;
    }

    public static StartResponse fromEntity(Battle battle) {
        return StartResponse.builder()
                .battleId(battle.getId())
                .playerId(battle.getPlayerId())
                .enemyId(battle.getEnemyId())
                .currentTurn(battle.getCurrentTurn().name())
                .playerHp(battle.getPlayerHp())
                .enemyHp(battle.getEnemyHp())
                .turnCount(battle.getTurnCount())
                .status(battle.getStatus().name())
                .build();
    }
}
