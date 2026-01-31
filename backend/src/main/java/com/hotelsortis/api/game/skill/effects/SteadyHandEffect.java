package com.hotelsortis.api.game.skill.effects;

import com.hotelsortis.api.game.skill.GameState;
import com.hotelsortis.api.game.skill.SkillEffect;
import com.hotelsortis.api.game.skill.SkillEffectException;
import com.hotelsortis.api.game.skill.SkillTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Steady Hand (안정된 손)
 *
 * Rarity: Common
 * Trigger: DICE_ROLL
 * Effect: 주사위 결과 중 1이 나오면 자동으로 2로 변경됩니다.
 *
 * DB: skills.id = 2
 * PROJECTPLAN.md 4.3.1절 참조
 */
@Slf4j
@Component
public class SteadyHandEffect implements SkillEffect {

    private static final Long SKILL_ID = 2L;
    private static final String SKILL_NAME = "Steady Hand";

    @Override
    public GameState apply(GameState state) throws SkillEffectException {
        try {
            int[] dice = state.getDiceCopy();

            if (dice == null || dice.length != 3) {
                throw new SkillEffectException(
                    SKILL_ID,
                    SKILL_NAME,
                    "Invalid dice array: " + (dice == null ? "null" : dice.length)
                );
            }

            int convertCount = 0;
            for (int i = 0; i < dice.length; i++) {
                if (dice[i] == 1) {
                    state.setDice(i, 2);
                    convertCount++;
                }
            }

            if (convertCount > 0) {
                log.info("Steady Hand: Converted {} dice (1 -> 2)", convertCount);
            }

            return state;

        } catch (Exception e) {
            throw new SkillEffectException(SKILL_ID, SKILL_NAME,
                "Failed to convert dice", e);
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
        return SkillTrigger.DICE_ROLL;
    }

    @Override
    public String getEffectDescription(GameState state) {
        int count = 0;
        for (int d : state.getDice()) {
            if (d == 2) count++;  // 변환된 주사위는 2
        }
        return String.format("%s: Converted %d die(s) from 1 to 2", SKILL_NAME, count);
    }
}
