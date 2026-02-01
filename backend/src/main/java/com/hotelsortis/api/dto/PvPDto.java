package com.hotelsortis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PvP 관련 DTO 모음
 */
public class PvPDto {

    /**
     * 매칭 대기 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinQueueRequest {
        private Long playerId;
    }

    /**
     * 매칭 대기 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinQueueResponse {
        private Long playerId;
        private Integer elo;
        private Long queueSize;
        private String status; // "WAITING", "ALREADY_IN_BATTLE"
        private Long battleId; // status가 "ALREADY_IN_BATTLE"일 때 전투 ID
    }

    /**
     * 매칭 찾기 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchFoundResponse {
        private Long battleId;
        private Long player1Id;
        private Long player2Id;
        private Integer player1Elo;
        private Integer player2Elo;
        private String status; // "MATCH_FOUND"
    }

    /**
     * 매칭 대기 취소 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveQueueRequest {
        private Long playerId;
    }

    /**
     * 랭크 정보 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankInfoResponse {
        private Long playerId;
        private Integer elo;
        private String tier; // "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER"
        private Integer wins;
        private Integer losses;
        private Integer draws;
        private Double winRate;
    }

    /**
     * PvP 보상 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardInfo {
        private Integer eloChange; // +25, -25, 0
        private Integer soulStones; // 20, 5, 10
        private String result; // "VICTORY", "DEFEAT", "DRAW"
    }

    /**
     * WebSocket 메시지: 턴 시작
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TurnStartMessage {
        private Long battleId;
        private String currentTurn; // "PLAYER" or "ENEMY"
        private Integer turnNumber;
        private Long timeRemaining; // 밀리초
    }

    /**
     * WebSocket 메시지: 주사위 결과
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiceResultMessage {
        private Long battleId;
        private Long playerId;
        private int[] dice;
        private String handRank;
        private String handRankKR;
        private Integer handPower;
        private Integer damage;
        private Integer opponentHp;
    }

    /**
     * WebSocket 메시지: 전투 종료
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BattleEndMessage {
        private Long battleId;
        private String result; // "VICTORY", "DEFEAT", "DRAW"
        private RewardInfo reward;
    }
}
