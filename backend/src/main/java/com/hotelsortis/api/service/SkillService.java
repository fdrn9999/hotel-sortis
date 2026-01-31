package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.SkillDto;
import com.hotelsortis.api.entity.Skill;
import com.hotelsortis.api.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 스킬 서비스 (i18n 지원)
 * CLAUDE.md i18n 규칙 참조
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;

    /**
     * 모든 스킬 조회 (사용자 언어에 맞게 변환)
     *
     * @param language 언어 코드 ("ko", "en", "ja", "zh")
     * @return 스킬 목록
     */
    @Transactional(readOnly = true)
    public SkillDto.SkillListResponse getAllSkills(String language) {
        log.info("Getting all skills for language: {}", language);

        // 유효한 언어인지 확인
        String validatedLang = validateLanguage(language);

        // DB에서 모든 스킬 조회
        List<Skill> skills = skillRepository.findAllOrderById();

        // 사용자 언어에 맞게 변환
        List<SkillDto.SkillResponse> skillResponses = skills.stream()
                .map(skill -> SkillDto.SkillResponse.fromEntity(skill, validatedLang))
                .collect(Collectors.toList());

        log.info("Found {} skills", skillResponses.size());

        return SkillDto.SkillListResponse.builder()
                .skills(skillResponses)
                .total(skillResponses.size())
                .language(validatedLang)
                .build();
    }

    /**
     * 희귀도별 스킬 조회
     *
     * @param rarity 희귀도
     * @param language 언어 코드
     * @return 스킬 목록
     */
    @Transactional(readOnly = true)
    public SkillDto.SkillListResponse getSkillsByRarity(String rarity, String language) {
        log.info("Getting skills by rarity: {} for language: {}", rarity, language);

        String validatedLang = validateLanguage(language);
        Skill.Rarity rarityEnum = Skill.Rarity.valueOf(rarity);

        List<Skill> skills = skillRepository.findByRarity(rarityEnum);

        List<SkillDto.SkillResponse> skillResponses = skills.stream()
                .map(skill -> SkillDto.SkillResponse.fromEntity(skill, validatedLang))
                .collect(Collectors.toList());

        return SkillDto.SkillListResponse.builder()
                .skills(skillResponses)
                .total(skillResponses.size())
                .language(validatedLang)
                .build();
    }

    /**
     * 특정 스킬 조회
     *
     * @param skillId 스킬 ID
     * @param language 언어 코드
     * @return 스킬 정보
     */
    @Transactional(readOnly = true)
    public SkillDto.SkillResponse getSkill(Long skillId, String language) {
        log.info("Getting skill {} for language: {}", skillId, language);

        String validatedLang = validateLanguage(language);

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + skillId));

        return SkillDto.SkillResponse.fromEntity(skill, validatedLang);
    }

    /**
     * 여러 스킬 ID로 조회 (스킬 장착 시 사용)
     *
     * @param skillIds 스킬 ID 목록
     * @param language 언어 코드
     * @return 스킬 목록
     */
    @Transactional(readOnly = true)
    public SkillDto.SkillListResponse getSkillsByIds(List<Long> skillIds, String language) {
        log.info("Getting skills by IDs: {} for language: {}", skillIds, language);

        if (skillIds == null || skillIds.isEmpty()) {
            return SkillDto.SkillListResponse.builder()
                    .skills(List.of())
                    .total(0)
                    .language(language)
                    .build();
        }

        // 최대 4개 검증 (CLAUDE.md 1.1.1절)
        if (skillIds.size() > 4) {
            throw new IllegalArgumentException("Cannot equip more than 4 skills");
        }

        String validatedLang = validateLanguage(language);

        List<Skill> skills = skillRepository.findByIds(skillIds);

        List<SkillDto.SkillResponse> skillResponses = skills.stream()
                .map(skill -> SkillDto.SkillResponse.fromEntity(skill, validatedLang))
                .collect(Collectors.toList());

        return SkillDto.SkillListResponse.builder()
                .skills(skillResponses)
                .total(skillResponses.size())
                .language(validatedLang)
                .build();
    }

    /**
     * 언어 코드 검증
     * 지원하는 언어: ko, en, ja, zh
     *
     * @param language 언어 코드
     * @return 검증된 언어 코드 (소문자)
     */
    private String validateLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            log.warn("Language not specified, defaulting to 'en'");
            return "en";
        }

        String lang = language.toLowerCase().trim();

        // 지원하는 언어 확인 (CLAUDE.md i18n 규칙)
        if (!List.of("ko", "en", "ja", "zh").contains(lang)) {
            log.warn("Unsupported language: {}, defaulting to 'en'", language);
            return "en";
        }

        return lang;
    }
}
