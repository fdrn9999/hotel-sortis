package com.hotelsortis.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelsortis.api.dto.BattleDto;
import com.hotelsortis.api.entity.Battle;
import com.hotelsortis.api.entity.Boss;
import com.hotelsortis.api.game.HandEvaluator;
import com.hotelsortis.api.game.skill.GameState;
import com.hotelsortis.api.game.skill.SkillEffectEngine;
import com.hotelsortis.api.game.skill.SkillTrigger;
import com.hotelsortis.api.repository.BattleRepository;
import com.hotelsortis.api.repository.BossRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BattleService {

    private final BattleRepository battleRepository;
    private final BossRepository bossRepository;
    private final HandEvaluator handEvaluator;
    private final SkillEffectEngine skillEffectEngine;
    private final MutatorService mutatorService;
    private final ObjectMapper objectMapper;

    /**
     * Start a new battle
     */
    @Transactional
    public BattleDto.StartResponse startBattle(BattleDto.StartRequest request) {
        // Validate max 4 skills
        if (request.getEquippedSkills() != null && request.getEquippedSkills().size() > 4) {
            throw new IllegalArgumentException("Maximum 4 skills allowed");
        }

        // Get initial HP based on mutator (endurance = 150, default = 100)
        String mutatorId = request.getMutatorId();
        int initialHp = mutatorService.getInitialHp(mutatorId);

        Battle battle = Battle.builder()
                .playerId(request.getPlayerId())
                .battleType(Battle.BattleType.valueOf(request.getBattleType()))
                .floor(request.getFloor())
                .bossId(request.getBossId())
                .bossPhase(request.getBossPhase() != null ? request.getBossPhase() : 1)
                .mutatorId(mutatorId)
                .playerHp(initialHp)
                .enemyHp(initialHp)
                .turnCount(1)
                .currentTurn(Battle.TurnActor.PLAYER)
                .status(Battle.Status.ONGOING)
                .playerEquippedSkills(request.getEquippedSkills() != null ?
                        request.getEquippedSkills().toString() : "[]")
                .enemyEquippedSkills(request.getEnemySkills() != null ?
                        request.getEnemySkills().toString() : "[]")
                .build();

        battle = battleRepository.save(battle);
        log.info("Battle started: id={}, playerId={}, floor={}, mutator={}, hp={}",
                battle.getId(), battle.getPlayerId(), battle.getFloor(), mutatorId, initialHp);

        return BattleDto.fromEntity(battle);
    }

    /**
     * Roll dice and process turn
     * IMPORTANT: Dice are generated SERVER-SIDE only!
     */
    @Transactional
    public BattleDto.RollResponse rollDice(Long battleId, BattleDto.RollRequest request) {
        Battle battle = battleRepository.findByIdAndPlayerId(battleId, request.getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));

        if (battle.getStatus() != Battle.Status.ONGOING) {
            throw new IllegalStateException("Battle is not ongoing");
        }

        if (battle.getCurrentTurn() != Battle.TurnActor.PLAYER) {
            throw new IllegalStateException("Not player's turn");
        }

        // Parse equipped skills
        List<Long> equippedSkills = parseEquippedSkills(battle.getPlayerEquippedSkills());
        log.debug("Player equipped skills: {}", equippedSkills);

        // Check if silence mutator blocks skills this turn
        String mutatorId = battle.getMutatorId();
        boolean skillsSilenced = !mutatorService.shouldSkillsExecute(mutatorId);

        // 0. BATTLE_START trigger (first turn only)
        if (battle.getTurnCount() == 1 && !skillsSilenced) {
            log.info("First turn - executing BATTLE_START skills");
            GameState initialState = buildGameState(battle, new int[]{0, 0, 0}, null, 0);
            skillEffectEngine.executeSkillsByTrigger(SkillTrigger.BATTLE_START, equippedSkills, initialState);
        }

        // 1. Roll dice (SERVER-SIDE!)
        int[] playerDice = handEvaluator.rollDice();
        String hash = generateDiceHash(playerDice);
        log.info("Player rolled dice: {}", Arrays.toString(playerDice));

        // 1.5. Apply mutator dice effects (gravity: 1-2 -> 3, chaos: re-roll one)
        playerDice = mutatorService.applyDiceRollMutator(mutatorId, playerDice);

        // 2. DICE_ROLL trigger - skills can modify dice (if not silenced)
        GameState state = buildGameState(battle, playerDice, null, 0);
        if (!skillsSilenced) {
            state = skillEffectEngine.executeSkillsByTrigger(SkillTrigger.DICE_ROLL, equippedSkills, state);
            playerDice = state.getDice();  // Get modified dice
            log.debug("After DICE_ROLL skills: dice={}", Arrays.toString(playerDice));
        }

        // 3. Evaluate hand
        HandEvaluator.HandResult handResult = handEvaluator.evaluate(playerDice);
        log.info("Player hand: {} (power={})", handResult.getRank(), handResult.getPower());

        // 4. BEFORE_DAMAGE trigger - skills can modify damage (if not silenced)
        state.setHand(handResult);
        state.setDamage(handResult.getPower());
        if (!skillsSilenced) {
            state = skillEffectEngine.executeSkillsByTrigger(SkillTrigger.BEFORE_DAMAGE, equippedSkills, state);
        }
        int playerDamage = state.getDamage();  // Get modified damage
        log.info("After BEFORE_DAMAGE: damage={}, silenced={}", playerDamage, skillsSilenced);

        // 5. Apply damage to enemy (shield absorbs first)
        int shieldAbsorbed = Math.min(playerDamage, battle.getEnemyShield());
        int actualDamage = playerDamage - shieldAbsorbed;
        int newEnemyShield = battle.getEnemyShield() - shieldAbsorbed;
        int newEnemyHp = Math.max(0, battle.getEnemyHp() - actualDamage);
        battle.setEnemyShield(newEnemyShield);
        battle.setEnemyHp(newEnemyHp);
        state.setEnemyShield(newEnemyShield);
        state.setEnemyHp(newEnemyHp);
        log.info("Damage to enemy: total={}, shieldAbsorbed={}, actualDamage={}", playerDamage, shieldAbsorbed, actualDamage);

        // 6. AFTER_DAMAGE trigger - skills can have post-damage effects (if not silenced)
        if (!skillsSilenced) {
            state = skillEffectEngine.executeSkillsByTrigger(SkillTrigger.AFTER_DAMAGE, equippedSkills, state);
        }

        log.info("Player turn complete: dice={}, hand={}, damage={}, enemyHp={}, silenced={}",
                Arrays.toString(playerDice), handResult.getRank(), playerDamage, newEnemyHp, skillsSilenced);

        // 5. Check if enemy defeated
        if (newEnemyHp <= 0) {
            // Boss phase transition check
            BattleDto.BossPhaseTransition phaseTransition = checkBossPhaseTransition(battle);
            if (phaseTransition != null) {
                // Boss has more phases - reset HP, advance phase
                battleRepository.save(battle);
                BattleDto.RollResponse response = buildRollResponse(
                        battle, playerDice, hash, handResult, playerDamage, null, skillsSilenced);
                response.setBossPhaseTransition(phaseTransition);
                return response;
            }

            // Final phase or non-boss: victory
            battle.setStatus(Battle.Status.VICTORY);
            battle.setEndedAt(LocalDateTime.now());
            battleRepository.save(battle);

            return buildRollResponse(battle, playerDice, hash, handResult, playerDamage, null, skillsSilenced);
        }

        // 6. Enemy turn (PvE AI)
        BattleDto.EnemyTurnResult enemyTurnResult = processEnemyTurn(battle);

        // 7. Check if player defeated
        if (battle.getPlayerHp() <= 0) {
            battle.setStatus(Battle.Status.DEFEAT);
            battle.setEndedAt(LocalDateTime.now());
        }

        // 8. Check turn limit (max 10 turns)
        if (battle.getTurnCount() >= 10 && battle.getStatus() == Battle.Status.ONGOING) {
            battle.setStatus(Battle.Status.DRAW);
            battle.setEndedAt(LocalDateTime.now());
        }

        // 9. Increment turn count and set back to player
        battle.setTurnCount(battle.getTurnCount() + 1);
        battle.setCurrentTurn(Battle.TurnActor.PLAYER);

        battleRepository.save(battle);

        return buildRollResponse(battle, playerDice, hash, handResult, playerDamage, enemyTurnResult, skillsSilenced);
    }

    /**
     * Process enemy AI turn
     */
    private BattleDto.EnemyTurnResult processEnemyTurn(Battle battle) {
        // Parse enemy equipped skills
        List<Long> enemySkills = parseEquippedSkills(battle.getEnemyEquippedSkills());
        log.debug("Enemy equipped skills: {}", enemySkills);

        // Check if silence mutator blocks skills this turn (enemy gets own roll)
        String mutatorId = battle.getMutatorId();
        boolean enemySkillsSilenced = !mutatorService.shouldSkillsExecute(mutatorId);

        // 1. Roll dice for enemy (SERVER-SIDE!)
        int[] enemyDice = handEvaluator.rollDice();
        log.info("Enemy rolled dice: {}", Arrays.toString(enemyDice));

        // 1.5. Apply mutator dice effects
        enemyDice = mutatorService.applyDiceRollMutator(mutatorId, enemyDice);

        // 2. DICE_ROLL trigger - enemy skills can modify dice (if not silenced)
        GameState state = buildGameState(battle, enemyDice, null, 0);
        if (!enemySkillsSilenced) {
            state = skillEffectEngine.executeSkillsByTrigger(SkillTrigger.DICE_ROLL, enemySkills, state);
            enemyDice = state.getDice();  // Get modified dice
            log.debug("After enemy DICE_ROLL skills: dice={}", Arrays.toString(enemyDice));
        }

        // 3. Evaluate enemy hand
        HandEvaluator.HandResult enemyHand = handEvaluator.evaluate(enemyDice);
        log.info("Enemy hand: {} (power={})", enemyHand.getRank(), enemyHand.getPower());

        // 4. BEFORE_DAMAGE trigger - enemy skills can modify damage (if not silenced)
        state.setHand(enemyHand);
        state.setDamage(enemyHand.getPower());
        if (!enemySkillsSilenced) {
            state = skillEffectEngine.executeSkillsByTrigger(SkillTrigger.BEFORE_DAMAGE, enemySkills, state);
        }
        int enemyDamage = state.getDamage();  // Get modified damage
        log.info("After enemy BEFORE_DAMAGE: damage={}, silenced={}", enemyDamage, enemySkillsSilenced);

        // 5. Apply damage to player (shield absorbs first)
        int playerShieldAbsorbed = Math.min(enemyDamage, battle.getPlayerShield());
        int actualDamageToPlayer = enemyDamage - playerShieldAbsorbed;
        int newPlayerShield = battle.getPlayerShield() - playerShieldAbsorbed;
        int newPlayerHp = Math.max(0, battle.getPlayerHp() - actualDamageToPlayer);
        battle.setPlayerShield(newPlayerShield);
        battle.setPlayerHp(newPlayerHp);
        state.setPlayerShield(newPlayerShield);
        state.setPlayerHp(newPlayerHp);
        log.info("Damage to player: total={}, shieldAbsorbed={}, actualDamage={}", enemyDamage, playerShieldAbsorbed, actualDamageToPlayer);

        // 6. AFTER_DAMAGE trigger - enemy skills can have post-damage effects (if not silenced)
        if (!enemySkillsSilenced) {
            state = skillEffectEngine.executeSkillsByTrigger(SkillTrigger.AFTER_DAMAGE, enemySkills, state);
        }

        log.info("Enemy turn complete: dice={}, hand={}, damage={}, playerHp={}, silenced={}",
                Arrays.toString(enemyDice), enemyHand.getRank(), enemyDamage, newPlayerHp, enemySkillsSilenced);

        return BattleDto.EnemyTurnResult.builder()
                .dice(enemyDice)
                .hand(BattleDto.HandResult.builder()
                        .rank(enemyHand.getRank().getNameEn())
                        .rankKR(enemyHand.getRankKR())
                        .power(enemyHand.getPower())
                        .build())
                .damage(enemyDamage)
                .build();
    }

    /**
     * Check if boss has more phases. If so, transition to next phase.
     * Returns BossPhaseTransition if transition happened, null otherwise.
     */
    private BattleDto.BossPhaseTransition checkBossPhaseTransition(Battle battle) {
        if (battle.getBossId() == null) return null;

        Boss boss = bossRepository.findById(battle.getBossId()).orElse(null);
        if (boss == null) return null;

        int currentPhase = battle.getBossPhase() != null ? battle.getBossPhase() : 1;
        if (currentPhase >= boss.getTotalPhases()) {
            return null; // Final phase defeated
        }

        // Transition to next phase
        int nextPhase = currentPhase + 1;
        battle.setBossPhase(nextPhase);
        battle.setEnemyHp(100); // HP resets to 100 per phase

        log.info("Boss {} phase transition: {} -> {} (totalPhases={})",
                battle.getBossId(), currentPhase, nextPhase, boss.getTotalPhases());

        // Get quote for the new phase (default to English)
        String quote = null;
        try {
            if (boss.getQuotes() != null) {
                var quotesNode = objectMapper.readTree(boss.getQuotes());
                String phaseKey = "phase" + nextPhase + "_en";
                if (quotesNode.has(phaseKey)) {
                    quote = quotesNode.get(phaseKey).asText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse boss quotes", e);
        }

        return BattleDto.BossPhaseTransition.builder()
                .bossId(battle.getBossId())
                .newPhase(nextPhase)
                .totalPhases(boss.getTotalPhases())
                .quote(quote)
                .build();
    }

    private BattleDto.RollResponse buildRollResponse(
            Battle battle,
            int[] playerDice,
            String hash,
            HandEvaluator.HandResult handResult,
            int damage,
            BattleDto.EnemyTurnResult enemyTurn,
            boolean skillsSilenced
    ) {
        return BattleDto.RollResponse.builder()
                .dice(playerDice)
                .hash(hash)
                .hand(BattleDto.HandResult.builder()
                        .rank(handResult.getRank().getNameEn())
                        .rankKR(handResult.getRankKR())
                        .power(handResult.getPower())
                        .build())
                .damage(damage)
                .playerHp(battle.getPlayerHp())
                .enemyHp(battle.getEnemyHp())
                .playerShield(battle.getPlayerShield())
                .enemyShield(battle.getEnemyShield())
                .currentTurn(battle.getCurrentTurn().name())
                .status(battle.getStatus().name())
                .enemyTurn(enemyTurn)
                .fogActive(mutatorService.shouldHideHandName(battle.getMutatorId()))
                .skillsSilenced(skillsSilenced)
                .build();
    }

    /**
     * Generate hash for dice result verification
     */
    private String generateDiceHash(int[] dice) {
        try {
            String data = Arrays.toString(dice) + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return "hash-error";
        }
    }

    /**
     * Parse equipped skill IDs from JSON string
     */
    private List<Long> parseEquippedSkills(String skillsJson) {
        if (skillsJson == null || skillsJson.trim().isEmpty() || skillsJson.equals("[]")) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(skillsJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("Failed to parse equipped skills: {}", skillsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * Build GameState from current battle state
     */
    private GameState buildGameState(Battle battle, int[] dice, HandEvaluator.HandResult hand, int damage) {
        return GameState.builder()
                .dice(dice)
                .hand(hand)
                .damage(damage)
                .playerHp(battle.getPlayerHp())
                .enemyHp(battle.getEnemyHp())
                .playerShield(battle.getPlayerShield())
                .enemyShield(battle.getEnemyShield())
                .turnCount(battle.getTurnCount())
                .currentTurn(battle.getCurrentTurn().name())
                .battleId(battle.getId())
                .playerId(battle.getPlayerId())
                .enemyId(battle.getEnemyId())
                .build();
    }

    /**
     * Get current battle status
     */
    public BattleDto.BattleStatus getBattleStatus(Long battleId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));

        return BattleDto.BattleStatus.builder()
                .battleId(battle.getId())
                .playerHp(battle.getPlayerHp())
                .enemyHp(battle.getEnemyHp())
                .playerShield(battle.getPlayerShield())
                .enemyShield(battle.getEnemyShield())
                .turnCount(battle.getTurnCount())
                .currentTurn(battle.getCurrentTurn().name())
                .status(battle.getStatus().name())
                .build();
    }

    /**
     * Create a new PvP battle between two players
     */
    @Transactional
    public Battle createPvPBattle(com.hotelsortis.api.entity.Player player1, com.hotelsortis.api.entity.Player player2) {
        // Player 1의 장착 스킬
        String player1SkillsJson = player1.getEquippedSkillIds();
        List<Long> player1Skills = Collections.emptyList();
        if (player1SkillsJson != null && !player1SkillsJson.isEmpty()) {
            try {
                player1Skills = objectMapper.readValue(player1SkillsJson, new TypeReference<List<Long>>() {});
            } catch (Exception e) {
                log.error("Failed to parse player1 skills JSON", e);
            }
        }

        // Player 2의 장착 스킬
        String player2SkillsJson = player2.getEquippedSkillIds();
        List<Long> player2Skills = Collections.emptyList();
        if (player2SkillsJson != null && !player2SkillsJson.isEmpty()) {
            try {
                player2Skills = objectMapper.readValue(player2SkillsJson, new TypeReference<List<Long>>() {});
            } catch (Exception e) {
                log.error("Failed to parse player2 skills JSON", e);
            }
        }

        // Convert skills to JSON
        String player1SkillsJsonStr = "[]";
        String player2SkillsJsonStr = "[]";
        try {
            player1SkillsJsonStr = objectMapper.writeValueAsString(player1Skills);
            player2SkillsJsonStr = objectMapper.writeValueAsString(player2Skills);
        } catch (Exception e) {
            log.error("Failed to convert skills to JSON", e);
        }

        Battle battle = Battle.builder()
                .playerId(player1.getId())
                .enemyId(player2.getId())
                .battleType(Battle.BattleType.PVP)
                .playerHp(100)
                .enemyHp(100)
                .turnCount(1)
                .currentTurn(Battle.TurnActor.PLAYER)
                .status(Battle.Status.ONGOING)
                .playerEquippedSkills(player1SkillsJsonStr)
                .enemyEquippedSkills(player2SkillsJsonStr)
                .build();

        battle = battleRepository.save(battle);

        log.info("Created PvP battle: {} - Player {} (ELO: {}) vs Player {} (ELO: {})",
                battle.getId(), player1.getId(), player1.getElo(), player2.getId(), player2.getElo());

        return battle;
    }

    /**
     * Create a new PvP battle with draft mode (skills selected during draft phase)
     */
    @Transactional
    public Battle createPvPBattleWithDraft(com.hotelsortis.api.entity.Player player1, com.hotelsortis.api.entity.Player player2) {
        Battle battle = Battle.builder()
                .playerId(player1.getId())
                .enemyId(player2.getId())
                .battleType(Battle.BattleType.PVP)
                .playerHp(100)
                .enemyHp(100)
                .turnCount(1)
                .currentTurn(Battle.TurnActor.PLAYER)
                .status(Battle.Status.ONGOING)
                .playerEquippedSkills("[]") // Empty until draft completes
                .enemyEquippedSkills("[]")
                .draftCompleted(false)
                .build();

        battle = battleRepository.save(battle);

        log.info("Created PvP battle with draft: {} - Player {} (ELO: {}) vs Player {} (ELO: {})",
                battle.getId(), player1.getId(), player1.getElo(), player2.getId(), player2.getElo());

        return battle;
    }
}
