package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.BattleDto;
import com.hotelsortis.api.entity.Battle;
import com.hotelsortis.api.game.HandEvaluator;
import com.hotelsortis.api.repository.BattleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class BattleService {

    private final BattleRepository battleRepository;
    private final HandEvaluator handEvaluator;

    /**
     * Start a new battle
     */
    @Transactional
    public BattleDto.StartResponse startBattle(BattleDto.StartRequest request) {
        // Validate max 4 skills
        if (request.getEquippedSkills() != null && request.getEquippedSkills().size() > 4) {
            throw new IllegalArgumentException("Maximum 4 skills allowed");
        }

        Battle battle = Battle.builder()
                .playerId(request.getPlayerId())
                .battleType(Battle.BattleType.valueOf(request.getBattleType()))
                .floor(request.getFloor())
                .playerHp(100)  // HP is always 100
                .enemyHp(100)   // HP is always 100
                .turnCount(1)
                .currentTurn(Battle.TurnActor.PLAYER)
                .status(Battle.Status.ONGOING)
                .playerEquippedSkills(request.getEquippedSkills() != null ?
                        request.getEquippedSkills().toString() : "[]")
                .build();

        battle = battleRepository.save(battle);
        log.info("Battle started: id={}, playerId={}, floor={}",
                battle.getId(), battle.getPlayerId(), battle.getFloor());

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

        // 1. Roll dice (SERVER-SIDE!)
        int[] playerDice = handEvaluator.rollDice();
        String hash = generateDiceHash(playerDice);

        // 2. Evaluate hand
        HandEvaluator.HandResult handResult = handEvaluator.evaluate(playerDice);

        // 3. Calculate damage (base damage = hand power)
        int playerDamage = handResult.getPower();

        // 4. Apply damage to enemy
        int newEnemyHp = Math.max(0, battle.getEnemyHp() - playerDamage);
        battle.setEnemyHp(newEnemyHp);

        log.info("Player rolled: dice={}, hand={}, power={}, enemyHp={}",
                Arrays.toString(playerDice), handResult.getRank(), handResult.getPower(), newEnemyHp);

        // 5. Check if enemy defeated
        if (newEnemyHp <= 0) {
            battle.setStatus(Battle.Status.VICTORY);
            battle.setEndedAt(LocalDateTime.now());
            battleRepository.save(battle);

            return buildRollResponse(battle, playerDice, hash, handResult, playerDamage, null);
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

        return buildRollResponse(battle, playerDice, hash, handResult, playerDamage, enemyTurnResult);
    }

    /**
     * Process enemy AI turn
     */
    private BattleDto.EnemyTurnResult processEnemyTurn(Battle battle) {
        // Roll dice for enemy (SERVER-SIDE!)
        int[] enemyDice = handEvaluator.rollDice();

        // Evaluate enemy hand
        HandEvaluator.HandResult enemyHand = handEvaluator.evaluate(enemyDice);

        // Calculate damage
        int enemyDamage = enemyHand.getPower();

        // Apply damage to player
        int newPlayerHp = Math.max(0, battle.getPlayerHp() - enemyDamage);
        battle.setPlayerHp(newPlayerHp);

        log.info("Enemy rolled: dice={}, hand={}, power={}, playerHp={}",
                Arrays.toString(enemyDice), enemyHand.getRank(), enemyHand.getPower(), newPlayerHp);

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

    private BattleDto.RollResponse buildRollResponse(
            Battle battle,
            int[] playerDice,
            String hash,
            HandEvaluator.HandResult handResult,
            int damage,
            BattleDto.EnemyTurnResult enemyTurn
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
                .currentTurn(battle.getCurrentTurn().name())
                .status(battle.getStatus().name())
                .enemyTurn(enemyTurn)
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
     * Get current battle status
     */
    public BattleDto.BattleStatus getBattleStatus(Long battleId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));

        return BattleDto.BattleStatus.builder()
                .battleId(battle.getId())
                .playerHp(battle.getPlayerHp())
                .enemyHp(battle.getEnemyHp())
                .turnCount(battle.getTurnCount())
                .currentTurn(battle.getCurrentTurn().name())
                .status(battle.getStatus().name())
                .build();
    }
}
