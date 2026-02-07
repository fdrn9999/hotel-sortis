package com.hotelsortis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileDto {
        private Long userId;
        private String email;
        private String username;
        private Long playerId;
        private Integer elo;
        private Integer soulStones;
        private Integer currentFloor;
        private Integer highestFloorCleared;
        private String preferredLanguage;
        private String role;
        private Boolean emailVerified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileDto {
        private String username;
        private String preferredLanguage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordDto {
        private String currentPassword;
        private String newPassword;
    }

    /**
     * 공개 프로필 DTO (타 플레이어 조회용)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerPublicProfileDto {
        private Long playerId;
        private String username;
        private Integer elo;
        private String tier;
        private Integer currentFloor;
        private Integer highestFloorCleared;
        private Integer wins;
        private Integer losses;
        private Integer draws;
        private Double winRate;
        private String avatarId;
    }

    /**
     * 플레이어 통계 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerStatsDto {
        private Integer wins;
        private Integer losses;
        private Integer draws;
        private Double winRate;
        private Integer totalMatches;
        private Integer currentWinStreak;
        private Integer bestWinStreak;
    }

    /**
     * 매치 히스토리 항목 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchHistoryEntryDto {
        private Long battleId;
        private Long opponentId;
        private String opponentName;
        private Integer opponentElo;
        private String result; // "VICTORY", "DEFEAT", "DRAW"
        private Integer eloChange;
        private String battleType; // "PVP"
        private java.time.LocalDateTime createdAt;
    }

    /**
     * 프로필 편집 DTO (아바타 포함)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileExtendedDto {
        private String username;
        private String preferredLanguage;
        private String avatarId;
    }
}
