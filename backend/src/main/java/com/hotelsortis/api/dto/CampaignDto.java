package com.hotelsortis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class CampaignDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignProgressResponse {
        private Long playerId;
        private Integer currentRunFloor;
        private Integer currentRunHp;
        private List<FloorStatus> floors;
        private Map<String, Boolean> bossesDefeated;
        private Integer totalRuns;
        private Integer totalVictories;
        private Integer totalDefeats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FloorStatus {
        private Integer floor;
        private String floorType;
        private Integer battleCount;
        private Boolean cleared;
        private Boolean locked;
        private String description;
        private Integer aiLevel;
        private String bossId;
        private String bossName;
        private String skillRewardRarity;
        // Mutator fields
        private String mutatorId;
        private String mutatorName;
        private String mutatorDescription;
        private String mutatorIcon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartFloorRequest {
        private Long playerId;
        private List<Long> equippedSkillIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartFloorResponse {
        private Long battleId;
        private Integer floor;
        private String floorType;
        private Integer battleIndex;
        private Integer totalBattles;
        private Integer aiLevel;
        private List<Long> enemySkillIds;
        private String bossId;
        private String bossName;
        private Integer bossPhase;
        private Integer bossTotalPhases;
        // Mutator fields
        private String mutatorId;
        private String mutatorName;
        private String mutatorDescription;
        private String mutatorIcon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FloorCompleteResponse {
        private Integer floor;
        private Boolean cleared;
        private Integer nextFloor;
        private Boolean skillRewardAvailable;
        private List<SkillRewardOption> offeredSkills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillRewardOption {
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
    public static class SelectSkillRewardRequest {
        private Long playerId;
        private Long selectedSkillId;
    }
}
