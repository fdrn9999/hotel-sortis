package com.hotelsortis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTOs for PvP Draft Mode
 *
 * Snake Draft Order: A→B→B→A→A→B→B→A (8 picks = 4 skills each)
 */
public class DraftDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DraftState {
        private Long battleId;
        private Long player1Id;
        private Long player2Id;
        private String currentTurn; // "player1" or "player2"
        private Integer pickNumber; // 1-8
        private Integer timeRemaining; // milliseconds
        private List<SkillInfo> pool; // Available skills
        private List<SkillInfo> player1Picks;
        private List<SkillInfo> player2Picks;
        private Boolean player1Ready;
        private Boolean player2Ready;
        private String status; // "IN_PROGRESS", "COMPLETED", "CANCELLED"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private Long skillId;
        private String skillCode;
        private String name;
        private String description;
        private String rarity;
        private String triggerType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DraftPool {
        private Long battleId;
        private List<SkillInfo> skills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickRequest {
        private Long playerId;
        private Long skillId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickResponse {
        private Long battleId;
        private Long playerId;
        private Long skillId;
        private String skillName;
        private Integer pickNumber;
        private String nextTurn; // "player1" or "player2" or null if complete
        private Boolean success;
        private String error; // null if success
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadyRequest {
        private Long playerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DraftCompleteMessage {
        private Long battleId;
        private List<Long> player1Skills;
        private List<Long> player2Skills;
        private String status; // "READY_TO_START"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimerUpdate {
        private Long battleId;
        private Integer timeRemaining; // milliseconds
        private String currentTurn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DraftStartMessage {
        private Long battleId;
        private Long player1Id;
        private Long player2Id;
        private String firstPick; // "player1" or "player2"
        private Integer timePerPick; // 30000 ms
        private Integer totalPicks; // 8
    }

    /**
     * Calculate whose turn it is based on snake draft order
     * Order: A→B→B→A→A→B→B→A (picks 1-8)
     *
     * @param pickNumber Current pick number (1-8)
     * @return "player1" or "player2"
     */
    public static String getSnakeDraftTurn(int pickNumber) {
        // Snake draft pattern: 1=A, 2=B, 3=B, 4=A, 5=A, 6=B, 7=B, 8=A
        // Pattern: pick 1 -> A, picks 2-3 -> B, picks 4-5 -> A, picks 6-7 -> B, pick 8 -> A
        return switch (pickNumber) {
            case 1, 4, 5, 8 -> "player1";
            case 2, 3, 6, 7 -> "player2";
            default -> throw new IllegalArgumentException("Invalid pick number: " + pickNumber);
        };
    }
}
