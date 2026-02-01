package com.hotelsortis.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bosses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Boss {

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

    @Column(nullable = false)
    private Integer floor;

    @Column(name = "total_phases", nullable = false)
    private Integer totalPhases;

    @Column(name = "phase_config", nullable = false, columnDefinition = "JSON")
    private String phaseConfig;

    @Column(columnDefinition = "JSON")
    private String quotes;

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
}
