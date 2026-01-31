package com.hotelsortis.api.game.skill;

import com.hotelsortis.api.game.skill.effects.HighRollerEffect;
import com.hotelsortis.api.game.skill.effects.LuckyRerollEffect;
import com.hotelsortis.api.game.skill.effects.PairMasterEffect;
import com.hotelsortis.api.game.skill.effects.SafeBetEffect;
import com.hotelsortis.api.game.skill.effects.SteadyHandEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 스킬 효과 등록 설정
 * 애플리케이션 시작 시 모든 스킬 효과를 SkillEffectEngine에 자동 등록
 *
 * CLAUDE.md 1.2절 참조
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SkillEffectConfiguration {

    private final SkillEffectEngine skillEffectEngine;

    /**
     * 애플리케이션 시작 시 모든 스킬 효과 등록
     */
    @Bean
    public CommandLineRunner registerSkillEffects(
        // Common 스킬 (기본 제공 3개)
        LuckyRerollEffect luckyReroll,
        SteadyHandEffect steadyHand,
        SafeBetEffect safeBet,
        // Rare 스킬 (5층 보스 보상)
        HighRollerEffect highRoller,
        PairMasterEffect pairMaster
        // TODO: 나머지 55개 스킬들도 여기에 추가
    ) {
        return args -> {
            log.info("Registering skill effects...");

            List<SkillEffect> effects = List.of(
                // Common (기본 제공 3개)
                luckyReroll,    // ID: 1
                steadyHand,     // ID: 2
                safeBet,        // ID: 3

                // Rare (5층 보스 보상)
                highRoller,     // ID: 4
                pairMaster      // ID: 5

                // TODO: 나머지 55개 스킬 추가
                // Epic (10층 보스 보상)
                // Legendary (15층 보스 보상)
            );

            skillEffectEngine.registerSkillEffects(effects);

            log.info("Successfully registered {} skill effects", effects.size());
            log.info("Registered skill IDs: {}", skillEffectEngine.getRegisteredSkillIds());
        };
    }
}
