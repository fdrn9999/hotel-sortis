package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dice_skins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiceSkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skin_code", nullable = false, unique = true, length = 50)
    private String skinCode;

    @Column(name = "name_ko", nullable = false, length = 100)
    private String nameKo;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_ja", nullable = false, length = 100)
    private String nameJa;

    @Column(name = "name_zh", nullable = false, length = 100)
    private String nameZh;

    @Column(name = "description_ko", nullable = false, columnDefinition = "TEXT")
    private String descriptionKo;

    @Column(name = "description_en", nullable = false, columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_ja", nullable = false, columnDefinition = "TEXT")
    private String descriptionJa;

    @Column(name = "description_zh", nullable = false, columnDefinition = "TEXT")
    private String descriptionZh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String material = "MeshStandardMaterial";

    @Column(name = "base_color", nullable = false, length = 7)
    private String baseColor;

    @Column(name = "pip_color", nullable = false, length = 7)
    private String pipColor;

    @Column(name = "texture_url")
    private String textureUrl;

    @Column(nullable = false)
    @Builder.Default
    private Float metalness = 0.0f;

    @Column(nullable = false)
    @Builder.Default
    private Float roughness = 0.5f;

    @Column(name = "emissive_color", length = 7)
    private String emissiveColor;

    @Column(name = "emissive_intensity")
    @Builder.Default
    private Float emissiveIntensity = 0.0f;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "preview_url")
    private String previewUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Rarity {
        Common, Rare, Epic, Legendary
    }
}
