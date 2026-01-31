package com.hotelsortis.api.game.skill.effects;

import com.hotelsortis.api.game.skill.GameState;
import com.hotelsortis.api.game.skill.SkillEffect;
import com.hotelsortis.api.game.skill.SkillEffectException;
import com.hotelsortis.api.game.skill.SkillTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Lucky Reroll (행운의 재굴림)
 *
 * Rarity: Common
 * Trigger: BATTLE_START
 * Effect: 매 전투 시작 시 주사위 1개를 자동으로 재굴림합니다.
 *
 * DB: skills.id = 1
 * PROJECTPLAN.md 4.3.1절 참조
 */
@Slf4j
@Component
public class LuckyRerollEffect implements SkillEffect {

    private static final Long SKILL_ID = 1L;
    private static final String SKILL_NAME = "Lucky Reroll";
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

            // 주사위 1개를 랜덤하게 재굴림
            int rerollIndex = random.nextInt(3);  // 0-2
            int oldValue = dice[rerollIndex];
            int newValue = random.nextInt(6) + 1;  // 1-6

            state.setDice(rerollIndex, newValue);

            log.info("Lucky Reroll: dice[{}] {} -> {}", rerollIndex, oldValue, newValue);

            return state;

        } catch (Exception e) {
            throw new SkillEffectException(SKILL_ID, SKILL_NAME,
                "Failed to reroll dice", e);
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
        return SkillTrigger.BATTLE_START;
    }

    @Override
    public String getEffectDescription(GameState state) {
        return String.format("%s: Rerolled 1 die", SKILL_NAME);
    }
}
