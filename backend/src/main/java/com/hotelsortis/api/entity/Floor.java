package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "floors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Floor {

    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "floor_type", nullable = false)
    private FloorType floorType;

    @Column(name = "battle_count", nullable = false)
    private Integer battleCount;

    @Column(name = "boss_id", length = 50)
    private String bossId;

    @Column(name = "mutator_id", length = 50)
    private String mutatorId;

    @Column(name = "ai_level", nullable = false)
    @Builder.Default
    private Integer aiLevel = 0;

    @Column(name = "enemy_skill_count", nullable = false)
    @Builder.Default
    private Integer enemySkillCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_reward_rarity")
    private SkillRarity skillRewardRarity;

    @Column(name = "description_ko")
    private String descriptionKo;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "description_ja")
    private String descriptionJa;

    @Column(name = "description_zh")
    private String descriptionZh;

    public enum FloorType {
        NORMAL, ELITE, BOSS
    }

    public enum SkillRarity {
        Common, Rare, Epic, Legendary
    }

    public String getDescription(String language) {
        return switch (language) {
            case "ko" -> descriptionKo;
            case "ja" -> descriptionJa;
            case "zh" -> descriptionZh;
            default -> descriptionEn;
        };
    }
}
