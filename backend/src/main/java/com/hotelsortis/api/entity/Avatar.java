package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "avatars")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Avatar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "avatar_code", nullable = false, unique = true, length = 50)
    private String avatarCode;

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

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl;

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
