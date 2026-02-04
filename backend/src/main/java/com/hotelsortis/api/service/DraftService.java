package com.hotelsortis.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelsortis.api.dto.DraftDto;
import com.hotelsortis.api.entity.Battle;
import com.hotelsortis.api.entity.Skill;
import com.hotelsortis.api.repository.BattleRepository;
import com.hotelsortis.api.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for managing PvP Draft Mode
 *
 * Uses Redis for draft state management with 10-minute TTL
 *
 * Snake Draft Order: A→B→B→A→A→B→B→A (8 picks = 4 skills each)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DraftService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SkillRepository skillRepository;
    private final BattleRepository battleRepository;
    private final ObjectMapper objectMapper;

    private static final int TIME_PER_PICK_MS = 30000; // 30 seconds
    private static final int TOTAL_PICKS = 8;
    private static final int DRAFT_TTL_MINUTES = 10;

    // Redis key prefixes
    private static final String DRAFT_STATE_KEY = "draft:%d:state";
    private static final String DRAFT_POOL_KEY = "draft:%d:pool";
    private static final String DRAFT_P1_PICKS_KEY = "draft:%d:player1:picks";
    private static final String DRAFT_P2_PICKS_KEY = "draft:%d:player2:picks";

    /**
     * Initialize a new draft session for a PvP battle
     */
    public DraftDto.DraftState initializeDraft(Long battleId, Long player1Id, Long player2Id, String lang) {
        log.info("Initializing draft for battle {} - Player {} vs Player {}", battleId, player1Id, player2Id);

        // Get all available skills from database
        List<Skill> allSkills = skillRepository.findAll();

        // Convert to SkillInfo with i18n
        List<DraftDto.SkillInfo> skillPool = allSkills.stream()
                .map(skill -> toSkillInfo(skill, lang))
                .collect(Collectors.toList());

        // Store skill pool in Redis
        try {
            String poolJson = objectMapper.writeValueAsString(
                    skillPool.stream().map(DraftDto.SkillInfo::getSkillId).collect(Collectors.toList())
            );
            redisTemplate.opsForValue().set(
                    String.format(DRAFT_POOL_KEY, battleId),
                    poolJson,
                    DRAFT_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize skill pool", e);
            throw new RuntimeException("Failed to initialize draft", e);
        }

        // Initialize draft state
        DraftDto.DraftState state = DraftDto.DraftState.builder()
                .battleId(battleId)
                .player1Id(player1Id)
                .player2Id(player2Id)
                .currentTurn("player1") // Player 1 (caller) picks first
                .pickNumber(1)
                .timeRemaining(TIME_PER_PICK_MS)
                .pool(skillPool)
                .player1Picks(new ArrayList<>())
                .player2Picks(new ArrayList<>())
                .player1Ready(false)
                .player2Ready(false)
                .status("IN_PROGRESS")
                .build();

        // Save state to Redis
        saveState(battleId, state);

        // Initialize empty pick lists
        redisTemplate.opsForValue().set(
                String.format(DRAFT_P1_PICKS_KEY, battleId),
                "[]",
                DRAFT_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        redisTemplate.opsForValue().set(
                String.format(DRAFT_P2_PICKS_KEY, battleId),
                "[]",
                DRAFT_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        log.info("Draft initialized: battleId={}, poolSize={}", battleId, skillPool.size());
        return state;
    }

    /**
     * Get current draft state
     */
    public DraftDto.DraftState getDraftState(Long battleId, String lang) {
        String stateJson = redisTemplate.opsForValue().get(String.format(DRAFT_STATE_KEY, battleId));
        if (stateJson == null) {
            log.warn("Draft state not found for battle {}", battleId);
            return null;
        }

        try {
            DraftDto.DraftState state = objectMapper.readValue(stateJson, DraftDto.DraftState.class);

            // Reload pool with i18n
            List<Long> poolIds = getPoolSkillIds(battleId);
            List<Skill> poolSkills = skillRepository.findAllById(poolIds);
            state.setPool(poolSkills.stream()
                    .map(skill -> toSkillInfo(skill, lang))
                    .collect(Collectors.toList()));

            // Reload picks with i18n
            state.setPlayer1Picks(getPlayerPicks(battleId, 1, lang));
            state.setPlayer2Picks(getPlayerPicks(battleId, 2, lang));

            return state;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize draft state for battle {}", battleId, e);
            return null;
        }
    }

    /**
     * Process a skill pick
     */
    public DraftDto.PickResponse pickSkill(Long battleId, Long playerId, Long skillId, String lang) {
        DraftDto.DraftState state = getDraftState(battleId, lang);
        if (state == null) {
            return DraftDto.PickResponse.builder()
                    .success(false)
                    .error("Draft not found")
                    .build();
        }

        // Validate status
        if (!"IN_PROGRESS".equals(state.getStatus())) {
            return DraftDto.PickResponse.builder()
                    .success(false)
                    .error("Draft is not in progress")
                    .build();
        }

        // Validate turn
        String expectedTurn = DraftDto.getSnakeDraftTurn(state.getPickNumber());
        boolean isPlayer1 = playerId.equals(state.getPlayer1Id());
        boolean isPlayer2 = playerId.equals(state.getPlayer2Id());

        if (!isPlayer1 && !isPlayer2) {
            return DraftDto.PickResponse.builder()
                    .success(false)
                    .error("Player not in this draft")
                    .build();
        }

        String playerTurn = isPlayer1 ? "player1" : "player2";
        if (!expectedTurn.equals(playerTurn)) {
            return DraftDto.PickResponse.builder()
                    .success(false)
                    .error("Not your turn")
                    .build();
        }

        // Validate skill availability
        List<Long> poolIds = getPoolSkillIds(battleId);
        if (!poolIds.contains(skillId)) {
            return DraftDto.PickResponse.builder()
                    .success(false)
                    .error("Skill not available")
                    .build();
        }

        // Remove skill from pool
        poolIds.remove(skillId);
        savePoolSkillIds(battleId, poolIds);

        // Add to player's picks
        int playerNum = isPlayer1 ? 1 : 2;
        addPlayerPick(battleId, playerNum, skillId);

        // Get skill name for response
        Skill skill = skillRepository.findById(skillId).orElse(null);
        String skillName = skill != null ? getSkillName(skill, lang) : "Unknown";

        // Advance to next pick
        int nextPickNumber = state.getPickNumber() + 1;
        String nextTurn = nextPickNumber <= TOTAL_PICKS ? DraftDto.getSnakeDraftTurn(nextPickNumber) : null;
        String newStatus = nextPickNumber > TOTAL_PICKS ? "PICKS_COMPLETE" : "IN_PROGRESS";

        // Update state
        state.setPickNumber(nextPickNumber);
        state.setCurrentTurn(nextTurn);
        state.setTimeRemaining(TIME_PER_PICK_MS);
        state.setStatus(newStatus);
        saveState(battleId, state);

        log.info("Draft pick: battleId={}, player={}, skill={}, pickNumber={}, nextTurn={}",
                battleId, playerId, skillId, state.getPickNumber() - 1, nextTurn);

        return DraftDto.PickResponse.builder()
                .battleId(battleId)
                .playerId(playerId)
                .skillId(skillId)
                .skillName(skillName)
                .pickNumber(state.getPickNumber() - 1)
                .nextTurn(nextTurn)
                .success(true)
                .build();
    }

    /**
     * Mark player as ready after draft picks complete
     */
    public boolean setPlayerReady(Long battleId, Long playerId) {
        DraftDto.DraftState state = getDraftState(battleId, "en");
        if (state == null || !"PICKS_COMPLETE".equals(state.getStatus())) {
            return false;
        }

        boolean isPlayer1 = playerId.equals(state.getPlayer1Id());
        if (isPlayer1) {
            state.setPlayer1Ready(true);
        } else if (playerId.equals(state.getPlayer2Id())) {
            state.setPlayer2Ready(true);
        } else {
            return false;
        }

        // Check if both ready
        if (Boolean.TRUE.equals(state.getPlayer1Ready()) && Boolean.TRUE.equals(state.getPlayer2Ready())) {
            state.setStatus("COMPLETED");
        }

        saveState(battleId, state);

        log.info("Player {} ready for battle {}, status={}", playerId, battleId, state.getStatus());
        return true;
    }

    /**
     * Check if draft is complete and both players are ready
     */
    public boolean isDraftComplete(Long battleId) {
        DraftDto.DraftState state = getDraftState(battleId, "en");
        return state != null && "COMPLETED".equals(state.getStatus());
    }

    /**
     * Finalize draft and update battle with selected skills
     */
    @Transactional
    public DraftDto.DraftCompleteMessage finalizeDraft(Long battleId) {
        DraftDto.DraftState state = getDraftState(battleId, "en");
        if (state == null) {
            throw new IllegalStateException("Draft not found for battle " + battleId);
        }

        // Get pick IDs
        List<Long> player1SkillIds = getPlayerPickIds(battleId, 1);
        List<Long> player2SkillIds = getPlayerPickIds(battleId, 2);

        // Update battle with skills
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalStateException("Battle not found: " + battleId));

        try {
            battle.setPlayerEquippedSkills(objectMapper.writeValueAsString(player1SkillIds));
            battle.setEnemyEquippedSkills(objectMapper.writeValueAsString(player2SkillIds));
            battle.setDraftCompleted(true);
            battleRepository.save(battle);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize skills", e);
        }

        // Clean up Redis keys
        cleanupDraft(battleId);

        log.info("Draft finalized: battleId={}, player1Skills={}, player2Skills={}",
                battleId, player1SkillIds, player2SkillIds);

        return DraftDto.DraftCompleteMessage.builder()
                .battleId(battleId)
                .player1Skills(player1SkillIds)
                .player2Skills(player2SkillIds)
                .status("READY_TO_START")
                .build();
    }

    /**
     * Handle timeout - auto-pick random skill for current player
     */
    public DraftDto.PickResponse handleTimeout(Long battleId, String lang) {
        DraftDto.DraftState state = getDraftState(battleId, lang);
        if (state == null || !"IN_PROGRESS".equals(state.getStatus())) {
            return null;
        }

        // Get random skill from pool
        List<Long> poolIds = getPoolSkillIds(battleId);
        if (poolIds.isEmpty()) {
            return null;
        }

        Long randomSkillId = poolIds.get(new Random().nextInt(poolIds.size()));

        // Determine current player
        String currentTurn = DraftDto.getSnakeDraftTurn(state.getPickNumber());
        Long currentPlayerId = "player1".equals(currentTurn) ? state.getPlayer1Id() : state.getPlayer2Id();

        log.info("Timeout auto-pick: battleId={}, player={}, skill={}", battleId, currentPlayerId, randomSkillId);

        return pickSkill(battleId, currentPlayerId, randomSkillId, lang);
    }

    // ============ Helper Methods ============

    private void saveState(Long battleId, DraftDto.DraftState state) {
        try {
            // Don't include pool in saved state (it's stored separately)
            DraftDto.DraftState stateToSave = DraftDto.DraftState.builder()
                    .battleId(state.getBattleId())
                    .player1Id(state.getPlayer1Id())
                    .player2Id(state.getPlayer2Id())
                    .currentTurn(state.getCurrentTurn())
                    .pickNumber(state.getPickNumber())
                    .timeRemaining(state.getTimeRemaining())
                    .player1Ready(state.getPlayer1Ready())
                    .player2Ready(state.getPlayer2Ready())
                    .status(state.getStatus())
                    .build();

            String json = objectMapper.writeValueAsString(stateToSave);
            redisTemplate.opsForValue().set(
                    String.format(DRAFT_STATE_KEY, battleId),
                    json,
                    DRAFT_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to save draft state", e);
        }
    }

    private List<Long> getPoolSkillIds(Long battleId) {
        String json = redisTemplate.opsForValue().get(String.format(DRAFT_POOL_KEY, battleId));
        if (json == null) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse pool skill IDs", e);
            return new ArrayList<>();
        }
    }

    private void savePoolSkillIds(Long battleId, List<Long> skillIds) {
        try {
            String json = objectMapper.writeValueAsString(skillIds);
            redisTemplate.opsForValue().set(
                    String.format(DRAFT_POOL_KEY, battleId),
                    json,
                    DRAFT_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to save pool skill IDs", e);
        }
    }

    private List<Long> getPlayerPickIds(Long battleId, int playerNum) {
        String key = playerNum == 1
                ? String.format(DRAFT_P1_PICKS_KEY, battleId)
                : String.format(DRAFT_P2_PICKS_KEY, battleId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse player picks", e);
            return new ArrayList<>();
        }
    }

    private List<DraftDto.SkillInfo> getPlayerPicks(Long battleId, int playerNum, String lang) {
        List<Long> pickIds = getPlayerPickIds(battleId, playerNum);
        if (pickIds.isEmpty()) return new ArrayList<>();

        List<Skill> skills = skillRepository.findAllById(pickIds);
        // Maintain order
        Map<Long, Skill> skillMap = skills.stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));
        return pickIds.stream()
                .map(id -> skillMap.get(id))
                .filter(Objects::nonNull)
                .map(skill -> toSkillInfo(skill, lang))
                .collect(Collectors.toList());
    }

    private void addPlayerPick(Long battleId, int playerNum, Long skillId) {
        List<Long> picks = getPlayerPickIds(battleId, playerNum);
        picks.add(skillId);
        try {
            String json = objectMapper.writeValueAsString(picks);
            String key = playerNum == 1
                    ? String.format(DRAFT_P1_PICKS_KEY, battleId)
                    : String.format(DRAFT_P2_PICKS_KEY, battleId);
            redisTemplate.opsForValue().set(key, json, DRAFT_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Failed to save player pick", e);
        }
    }

    private void cleanupDraft(Long battleId) {
        redisTemplate.delete(String.format(DRAFT_STATE_KEY, battleId));
        redisTemplate.delete(String.format(DRAFT_POOL_KEY, battleId));
        redisTemplate.delete(String.format(DRAFT_P1_PICKS_KEY, battleId));
        redisTemplate.delete(String.format(DRAFT_P2_PICKS_KEY, battleId));
    }

    private DraftDto.SkillInfo toSkillInfo(Skill skill, String lang) {
        return DraftDto.SkillInfo.builder()
                .skillId(skill.getId())
                .skillCode(skill.getSkillCode())
                .name(getSkillName(skill, lang))
                .description(getSkillDescription(skill, lang))
                .rarity(skill.getRarity())
                .triggerType(skill.getTriggerType())
                .build();
    }

    private String getSkillName(Skill skill, String lang) {
        return switch (lang) {
            case "en" -> skill.getNameEn();
            case "ja" -> skill.getNameJa();
            case "zh" -> skill.getNameZh();
            default -> skill.getNameKo();
        };
    }

    private String getSkillDescription(Skill skill, String lang) {
        return switch (lang) {
            case "en" -> skill.getDescEn();
            case "ja" -> skill.getDescJa();
            case "zh" -> skill.getDescZh();
            default -> skill.getDescKo();
        };
    }
}
