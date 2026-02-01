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
}
