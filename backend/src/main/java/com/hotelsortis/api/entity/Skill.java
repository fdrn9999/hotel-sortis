package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill_code", nullable = false, unique = true, length = 50)
    private String skillCode;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_ko", nullable = false, length = 100)
    private String nameKo;

    @Column(name = "name_ja", nullable = false, length = 100)
    private String nameJa;

    @Column(name = "name_zh", nullable = false, length = 100)
    private String nameZh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity;

    @Column(name = "description_en", nullable = false, columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_ko", nullable = false, columnDefinition = "TEXT")
    private String descriptionKo;

    @Column(name = "description_ja", nullable = false, columnDefinition = "TEXT")
    private String descriptionJa;

    @Column(name = "description_zh", nullable = false, columnDefinition = "TEXT")
    private String descriptionZh;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    @Column(name = "effect_json", nullable = false, columnDefinition = "JSON")
    private String effectJson;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Rarity {
        Common, Rare, Epic, Legendary
    }

    public enum TriggerType {
        BATTLE_START, TURN_START, DICE_ROLL, BEFORE_DAMAGE, AFTER_DAMAGE, PASSIVE
    }
}
