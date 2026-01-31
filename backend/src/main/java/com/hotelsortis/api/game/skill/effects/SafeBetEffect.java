package com.hotelsortis.api.game.skill.effects;

import com.hotelsortis.api.game.HandEvaluator.HandResult;
import com.hotelsortis.api.game.skill.GameState;
import com.hotelsortis.api.game.skill.SkillEffect;
import com.hotelsortis.api.game.skill.SkillEffectException;
import com.hotelsortis.api.game.skill.SkillTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Safe Bet (안전한 베팅)
 *
 * Rarity: Common
 * Trigger: BEFORE_DAMAGE
 * Effect: 노 핸드 족보일 때 데미지를 2배로 받습니다.
 *
 * DB: skills.id = 3
 * PROJECTPLAN.md 4.3.1절 참조
 */
@Slf4j
@Component
public class SafeBetEffect implements SkillEffect {

    private static final Long SKILL_ID = 3L;
    private static final String SKILL_NAME = "Safe Bet";

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

            // 노 핸드일 때만 효과 발동
            if ("NoHand".equalsIgnoreCase(hand.getRank())) {
                int originalDamage = state.getDamage();
                int newDamage = originalDamage * 2;

                state.setDamage(newDamage);

                log.info("Safe Bet: NoHand damage {} -> {} (2x)",
                    originalDamage, newDamage);
            }

            return state;

        } catch (Exception e) {
            throw new SkillEffectException(SKILL_ID, SKILL_NAME,
                "Failed to apply damage multiplier", e);
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
        // 노 핸드일 때만 발동
        HandResult hand = state.getHand();
        return hand != null && "NoHand".equalsIgnoreCase(hand.getRank());
    }

    @Override
    public String getEffectDescription(GameState state) {
        if (canApply(state)) {
            return String.format("%s: NoHand damage x2 (%d -> %d)",
                SKILL_NAME, state.getDamage() / 2, state.getDamage());
        }
        return String.format("%s: No effect (not NoHand)", SKILL_NAME);
    }
}
