package com.hotelsortis.api.game.skill.effects;

import com.hotelsortis.api.game.HandEvaluator.HandResult;
import com.hotelsortis.api.game.skill.GameState;
import com.hotelsortis.api.game.skill.SkillEffect;
import com.hotelsortis.api.game.skill.SkillEffectException;
import com.hotelsortis.api.game.skill.SkillTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Pair Master (페어 마스터)
 *
 * Rarity: Rare
 * Trigger: BEFORE_DAMAGE
 * Effect: 페어 족보 완성 시 공격력 +7 추가.
 *
 * DB: skills.id = 5
 * PROJECTPLAN.md 4.3.2절 참조
 * TASKS.md Phase 3.6 밸런스 조정 - 원래 +10에서 +7로 하향 (~70%)
 */
@Slf4j
@Component
public class PairMasterEffect implements SkillEffect {

    private static final Long SKILL_ID = 5L;
    private static final String SKILL_NAME = "Pair Master";
    private static final int DAMAGE_BONUS = 7;  // Phase 3.6 밸런스 조정

    @Override
    public GameState apply(GameState state) throws SkillEffectException {
        try {
            HandResult hand = state.getHand();

            if (hand == null) {
                throw new SkillEffectException(
                    SKILL_ID,
                    SKILL_NAME,
                    "Hand result is null (must be evaluated before BEFORE_DAMAGE)"
                );
            }

            // 페어일 때만 효과 발동
            if ("Pair".equalsIgnoreCase(hand.getRank())) {
                int originalDamage = state.getDamage();
                int newDamage = originalDamage + DAMAGE_BONUS;

                state.setDamage(newDamage);

                log.info("Pair Master: Pair damage {} -> {} (+{})",
                    originalDamage, newDamage, DAMAGE_BONUS);
            }

            return state;

        } catch (Exception e) {
            throw new SkillEffectException(SKILL_ID, SKILL_NAME,
                "Failed to apply damage bonus", e);
        }
    }

    @Override
    public Long getSkillId() {
        return SKILL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public SkillTrigger getTrigger() {
        return SkillTrigger.BEFORE_DAMAGE;
    }

    @Override
    public boolean canApply(GameState state) {
        // 페어일 때만 발동
        HandResult hand = state.getHand();
        return hand != null && "Pair".equalsIgnoreCase(hand.getRank());
    }

    @Override
    public String getEffectDescription(GameState state) {
        if (canApply(state)) {
            return String.format("%s: Pair bonus +%d damage", SKILL_NAME, DAMAGE_BONUS);
        }
        return String.format("%s: No effect (not Pair)", SKILL_NAME);
    }
}
