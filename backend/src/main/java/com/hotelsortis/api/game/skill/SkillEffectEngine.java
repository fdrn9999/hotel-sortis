package com.hotelsortis.api.game.skill;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 스킬 효과 엔진
 * 모든 스킬 효과를 관리하고 실행하는 중앙 서비스
 *
 * CLAUDE.md 1.2절 참조
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillEffectEngine {

    /**
     * 스킬 ID -> SkillEffect 매핑
     * 초기화 시 모든 스킬 효과를 등록
     */
    private final Map<Long, SkillEffect> skillEffectRegistry = new HashMap<>();

    /**
     * 스킬 효과 등록
     * 애플리케이션 시작 시 또는 동적으로 스킬 추가 시 호출
     *
     * @param effect 등록할 스킬 효과
     */
    public void registerSkillEffect(SkillEffect effect) {
        Long skillId = effect.getSkillId();

        if (skillEffectRegistry.containsKey(skillId)) {
            log.warn("Skill effect already registered: {} ({}). Overwriting.",
                skillId, effect.getSkillName());
        }

        skillEffectRegistry.put(skillId, effect);
        log.info("Registered skill effect: {} ({})", skillId, effect.getSkillName());
    }

    /**
     * 여러 스킬 효과 일괄 등록
     *
     * @param effects 등록할 스킬 효과 목록
     */
    public void registerSkillEffects(List<SkillEffect> effects) {
        effects.forEach(this::registerSkillEffect);
    }

    /**
     * 특정 트리거에 해당하는 스킬들을 실행
     *
     * @param trigger 스킬 트리거 타입
     * @param equippedSkillIds 플레이어가 장착한 스킬 ID 목록 (최대 4개)
     * @param state 현재 게임 상태
     * @return 스킬 효과가 적용된 게임 상태
     */
    public GameState executeSkillsByTrigger(
        SkillTrigger trigger,
        List<Long> equippedSkillIds,
        GameState state
    ) {
        log.debug("Executing skills for trigger: {} (equipped: {})", trigger, equippedSkillIds);

        if (equippedSkillIds == null || equippedSkillIds.isEmpty()) {
            log.debug("No skills equipped");
            return state;
        }

        // 최대 4개 스킬만 허용 (CLAUDE.md 1.1.1절)
        if (equippedSkillIds.size() > 4) {
            log.error("Too many skills equipped: {}. Max 4 allowed.", equippedSkillIds.size());
            throw new IllegalArgumentException("Cannot equip more than 4 skills");
        }

        // 해당 트리거를 가진 스킬들만 필터링
        List<SkillEffect> applicableSkills = equippedSkillIds.stream()
            .map(skillEffectRegistry::get)
            .filter(effect -> effect != null)
            .filter(effect -> effect.getTrigger() == trigger)
            .collect(Collectors.toList());

        log.debug("Found {} applicable skills for trigger {}", applicableSkills.size(), trigger);

        // 각 스킬 순차 실행
        GameState currentState = state;
        for (SkillEffect effect : applicableSkills) {
            try {
                // 이미 발동된 스킬인지 확인 (중복 발동 방지)
                if (currentState.hasSkillTriggered(effect.getSkillId())) {
                    log.debug("Skill {} already triggered this turn, skipping",
                        effect.getSkillName());
                    continue;
                }

                // 발동 조건 확인
                if (!effect.canApply(currentState)) {
                    log.debug("Skill {} cannot be applied (condition not met)",
                        effect.getSkillName());
                    continue;
                }

                // 스킬 효과 실행
                log.info("Applying skill: {} ({})", effect.getSkillId(), effect.getSkillName());
                GameState newState = effect.apply(currentState);

                // 스킬 발동 기록
                newState.markSkillTriggered(effect.getSkillId());

                // 효과 설명 로그
                String description = effect.getEffectDescription(newState);
                log.info("Skill effect: {}", description);

                currentState = newState;

            } catch (SkillEffectException e) {
                log.error("Skill effect error: {}", e.getMessage(), e);
                // 에러가 발생해도 다음 스킬은 계속 실행

            } catch (Exception e) {
                log.error("Unexpected error applying skill {} ({})",
                    effect.getSkillId(), effect.getSkillName(), e);
                // 예상치 못한 에러도 무시하고 계속 진행
            }
        }

        return currentState;
    }

    /**
     * 특정 스킬 효과 단독 실행 (테스트용)
     *
     * @param skillId 스킬 ID
     * @param state 현재 게임 상태
     * @return 스킬 효과가 적용된 게임 상태
     */
    public GameState executeSingleSkill(Long skillId, GameState state) {
        SkillEffect effect = skillEffectRegistry.get(skillId);

        if (effect == null) {
            log.error("Skill effect not found: {}", skillId);
            throw new IllegalArgumentException("Skill effect not registered: " + skillId);
        }

        if (!effect.canApply(state)) {
            log.warn("Skill {} cannot be applied (condition not met)", effect.getSkillName());
            return state;
        }

        return effect.apply(state);
    }

    /**
     * 등록된 스킬 효과 개수 반환
     */
    public int getRegisteredSkillCount() {
        return skillEffectRegistry.size();
    }

    /**
     * 특정 스킬이 등록되어 있는지 확인
     */
    public boolean isSkillRegistered(Long skillId) {
        return skillEffectRegistry.containsKey(skillId);
    }

    /**
     * 모든 등록된 스킬 ID 목록 반환
     */
    public List<Long> getRegisteredSkillIds() {
        return new ArrayList<>(skillEffectRegistry.keySet());
    }
}
