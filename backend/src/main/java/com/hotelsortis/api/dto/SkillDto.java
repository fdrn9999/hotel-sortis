package com.hotelsortis.api.dto;

import com.hotelsortis.api.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 스킬 DTO (i18n 지원)
 * CLAUDE.md i18n 규칙 참조
 */
public class SkillDto {

    /**
     * 스킬 목록 응답 (사용자 언어에 맞게 변환됨)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillResponse {
        private Long id;
        private String skillCode;
        private String name;          // 사용자 언어의 이름
        private String description;   // 사용자 언어의 설명
        private String rarity;
        private String triggerType;
        private String iconUrl;

        /**
         * Skill 엔티티를 사용자 언어에 맞게 변환
         *
         * @param skill Skill 엔티티
         * @param lang 언어 코드 ("ko", "en", "ja", "zh")
         * @return 변환된 SkillResponse
         */
        public static SkillResponse fromEntity(Skill skill, String lang) {
            String name;
            String description;

            // 언어에 따라 적절한 필드 선택
            switch (lang.toLowerCase()) {
                case "ko":
                    name = skill.getNameKo();
                    description = skill.getDescriptionKo();
                    break;
                case "ja":
                    name = skill.getNameJa();
                    description = skill.getDescriptionJa();
                    break;
                case "zh":
                    name = skill.getNameZh();
                    description = skill.getDescriptionZh();
                    break;
                case "en":
                default:
                    name = skill.getNameEn();
                    description = skill.getDescriptionEn();
                    break;
            }

            return SkillResponse.builder()
                    .id(skill.getId())
                    .skillCode(skill.getSkillCode())
                    .name(name)
                    .description(description)
                    .rarity(skill.getRarity().name())
                    .triggerType(skill.getTriggerType().name())
                    .iconUrl(skill.getIconUrl())
                    .build();
        }
    }

    /**
     * 스킬 목록 조회 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillListResponse {
        private List<SkillResponse> skills;
        private Integer total;
        private String language;  // 응답에 사용된 언어
    }
}
