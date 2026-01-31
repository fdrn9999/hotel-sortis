package com.hotelsortis.api.game.skill.effects;

import com.hotelsortis.api.game.skill.GameState;
import com.hotelsortis.api.game.skill.SkillEffect;
import com.hotelsortis.api.game.skill.SkillEffectException;
import com.hotelsortis.api.game.skill.SkillTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * High Roller (하이 롤러)
 *
 * Rarity: Rare
 * Trigger: DICE_ROLL
 * Effect: 주사위 1개가 항상 4 이상으로 나옵니다.
 *
 * DB: skills.id = 4
 * PROJECTPLAN.md 4.3.2절 참조
 */
@Slf4j
@Component
public class HighRollerEffect implements SkillEffect {

    private static final Long SKILL_ID = 4L;
    private static final String SKILL_NAME = "High Roller";
    private final Random random = new Random();

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

            // 가장 낮은 주사위 찾기
            int lowestValue = Math.min(dice[0], Math.min(dice[1], dice[2]));
            int lowestIndex = -1;

            for (int i = 0; i < dice.length; i++) {
                if (dice[i] == lowestValue) {
                    lowestIndex = i;
                    break;
                }
            }

            // 4 미만이면 4-6으로 변경
            if (dice[lowestIndex] < 4) {
                int oldValue = dice[lowestIndex];
                int newValue = random.nextInt(3) + 4;  // 4-6

                state.setDice(lowestIndex, newValue);

                log.info("High Roller: dice[{}] {} -> {} (forced 4+)",
                    lowestIndex, oldValue, newValue);
            }

            return state;

        } catch (Exception e) {
            throw new SkillEffectException(SKILL_ID, SKILL_NAME,
                "Failed to apply high roller effect", e);
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
        return String.format("%s: Ensured 1 die >= 4", SKILL_NAME);
    }
}
