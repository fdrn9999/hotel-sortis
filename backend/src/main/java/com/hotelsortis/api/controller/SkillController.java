package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.SkillDto;
import com.hotelsortis.api.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 스킬 API 컨트롤러 (i18n 지원)
 * CLAUDE.md i18n 규칙 참조
 */
@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")  // CORS 허용 (개발용)
public class SkillController {

    private final SkillService skillService;

    /**
     * 모든 스킬 조회
     * Accept-Language 헤더로 언어 지정
     *
     * GET /api/v1/skills
     * Header: Accept-Language: ko (or en, ja, zh)
     *
     * @param acceptLanguage Accept-Language 헤더
     * @return 스킬 목록
     */
    @GetMapping
    public ResponseEntity<SkillDto.SkillListResponse> getAllSkills(
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage
    ) {
        log.info("GET /api/v1/skills - Accept-Language: {}", acceptLanguage);

        // Accept-Language에서 첫 번째 언어 코드 추출
        // 예: "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7" -> "ko"
        String language = extractLanguageCode(acceptLanguage);

        SkillDto.SkillListResponse response = skillService.getAllSkills(language);

        return ResponseEntity.ok(response);
    }

    /**
     * 희귀도별 스킬 조회
     *
     * GET /api/v1/skills/rarity/{rarity}
     *
     * @param rarity 희귀도 (Common, Rare, Epic, Legendary)
     * @param acceptLanguage Accept-Language 헤더
     * @return 스킬 목록
     */
    @GetMapping("/rarity/{rarity}")
    public ResponseEntity<SkillDto.SkillListResponse> getSkillsByRarity(
            @PathVariable String rarity,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage
    ) {
        log.info("GET /api/v1/skills/rarity/{} - Accept-Language: {}", rarity, acceptLanguage);

        String language = extractLanguageCode(acceptLanguage);

        SkillDto.SkillListResponse response = skillService.getSkillsByRarity(rarity, language);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 스킬 조회
     *
     * GET /api/v1/skills/{id}
     *
     * @param id 스킬 ID
     * @param acceptLanguage Accept-Language 헤더
     * @return 스킬 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<SkillDto.SkillResponse> getSkill(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage
    ) {
        log.info("GET /api/v1/skills/{} - Accept-Language: {}", id, acceptLanguage);

        String language = extractLanguageCode(acceptLanguage);

        SkillDto.SkillResponse response = skillService.getSkill(id, language);

        return ResponseEntity.ok(response);
    }

    /**
     * 여러 스킬 ID로 조회 (스킬 장착 시 사용)
     *
     * GET /api/v1/skills/batch?ids=1,2,3,4
     *
     * @param ids 스킬 ID 목록 (쉼표로 구분)
     * @param acceptLanguage Accept-Language 헤더
     * @return 스킬 목록
     */
    @GetMapping("/batch")
    public ResponseEntity<SkillDto.SkillListResponse> getSkillsByIds(
            @RequestParam List<Long> ids,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage
    ) {
        log.info("GET /api/v1/skills/batch?ids={} - Accept-Language: {}", ids, acceptLanguage);

        String language = extractLanguageCode(acceptLanguage);

        SkillDto.SkillListResponse response = skillService.getSkillsByIds(ids, language);

        return ResponseEntity.ok(response);
    }

    /**
     * Accept-Language 헤더에서 언어 코드 추출
     *
     * @param acceptLanguage Accept-Language 헤더
     *                       예: "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"
     * @return 언어 코드 (예: "ko")
     */
    private String extractLanguageCode(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.trim().isEmpty()) {
            return "en";
        }

        // 첫 번째 언어 코드 추출
        String firstLang = acceptLanguage.split(",")[0].trim();

        // "ko-KR" -> "ko"
        if (firstLang.contains("-")) {
            return firstLang.split("-")[0].toLowerCase();
        }

        // "ko;q=0.9" -> "ko"
        if (firstLang.contains(";")) {
            return firstLang.split(";")[0].toLowerCase();
        }

        return firstLang.toLowerCase();
    }
}
