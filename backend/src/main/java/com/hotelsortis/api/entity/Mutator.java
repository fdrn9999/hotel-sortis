package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mutators")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mutator {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_ko", nullable = false, length = 100)
    private String nameKo;

    @Column(name = "name_ja", nullable = false, length = 100)
    private String nameJa;

    @Column(name = "name_zh", nullable = false, length = 100)
    private String nameZh;

    @Column(name = "description_en", nullable = false, columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_ko", nullable = false, columnDefinition = "TEXT")
    private String descriptionKo;

    @Column(name = "description_ja", nullable = false, columnDefinition = "TEXT")
    private String descriptionJa;

    @Column(name = "description_zh", nullable = false, columnDefinition = "TEXT")
    private String descriptionZh;

    @Column(name = "effect_json", nullable = false, columnDefinition = "JSON")
    private String effectJson;

    @Column(length = 50)
    private String icon;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getName(String language) {
        return switch (language) {
            case "ko" -> nameKo;
            case "ja" -> nameJa;
            case "zh" -> nameZh;
            default -> nameEn;
        };
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
