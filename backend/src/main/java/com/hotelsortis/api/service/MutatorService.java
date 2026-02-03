package com.hotelsortis.api.service;

import com.hotelsortis.api.entity.Mutator;
import com.hotelsortis.api.repository.MutatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service for handling floor mutator effects.
 *
 * Mutator effects:
 * - gravity: Dice values 1-2 become 3
 * - fog: Hide hand name (frontend only)
 * - silence: 50% chance skills don't activate
 * - chaos: Re-roll one random dice each turn
 * - endurance: HP starts at 150 instead of 100
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MutatorService {

    private final MutatorRepository mutatorRepository;
    private final Random random = new Random();

    /**
     * Get initial HP based on mutator (endurance mutator = 150 HP)
     */
    public int getInitialHp(String mutatorId) {
        if ("endurance".equals(mutatorId)) {
            log.info("Endurance mutator active: HP set to 150");
            return 150;
        }
        return 100;
    }

    /**
     * Apply DICE_ROLL phase mutator effects to dice array.
     * Returns the modified dice.
     */
    public int[] applyDiceRollMutator(String mutatorId, int[] dice) {
        if (mutatorId == null || dice == null) {
            return dice;
        }

        int[] modifiedDice = dice.clone();

        switch (mutatorId) {
            case "gravity" -> {
                // Dice values 1-2 become 3
                for (int i = 0; i < modifiedDice.length; i++) {
                    if (modifiedDice[i] == 1 || modifiedDice[i] == 2) {
                        log.debug("Gravity mutator: dice[{}] {} -> 3", i, modifiedDice[i]);
                        modifiedDice[i] = 3;
                    }
                }
                log.info("Gravity mutator applied: {} -> {}",
                    java.util.Arrays.toString(dice), java.util.Arrays.toString(modifiedDice));
            }
            case "chaos" -> {
                // Re-roll one random dice
                int rerollIndex = random.nextInt(3);
                int oldValue = modifiedDice[rerollIndex];
                modifiedDice[rerollIndex] = random.nextInt(6) + 1;
                log.info("Chaos mutator applied: dice[{}] {} -> {}",
                    rerollIndex, oldValue, modifiedDice[rerollIndex]);
            }
            default -> {
                // No dice modification for other mutators
            }
        }

        return modifiedDice;
    }

    /**
     * Check if skills should be silenced (silence mutator has 50% chance to block skills)
     * Returns true if skills should execute, false if silenced.
     */
    public boolean shouldSkillsExecute(String mutatorId) {
        if ("silence".equals(mutatorId)) {
            boolean execute = random.nextBoolean();
            log.info("Silence mutator: skills {} execute", execute ? "WILL" : "WILL NOT");
            return execute;
        }
        return true;
    }

    /**
     * Check if hand name should be hidden (fog mutator)
     * This is primarily handled on frontend, but we provide the check here too.
     */
    public boolean shouldHideHandName(String mutatorId) {
        return "fog".equals(mutatorId);
    }

    /**
     * Get mutator by ID
     */
    public Mutator getMutator(String mutatorId) {
        if (mutatorId == null) {
            return null;
        }
        return mutatorRepository.findById(mutatorId).orElse(null);
    }

    /**
     * Check if a mutator ID is valid
     */
    public boolean isValidMutator(String mutatorId) {
        if (mutatorId == null) {
            return true; // null is valid (no mutator)
        }
        return mutatorRepository.existsById(mutatorId);
    }
}
