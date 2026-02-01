package com.hotelsortis.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelsortis.api.dto.BattleDto;
import com.hotelsortis.api.dto.CampaignDto;
import com.hotelsortis.api.entity.*;
import com.hotelsortis.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private final FloorRepository floorRepository;
    private final BossRepository bossRepository;
    private final CampaignProgressRepository campaignProgressRepository;
    private final BattleRepository battleRepository;
    private final BattleService battleService;
    private final SkillRepository skillRepository;
    private final PlayerSkillRepository playerSkillRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get campaign progress for a player
     */
    @Transactional(readOnly = true)
    public CampaignDto.CampaignProgressResponse getCampaignProgress(Long playerId, String language) {
        CampaignProgress progress = getOrCreateProgress(playerId);
        List<Floor> floors = floorRepository.findAllByOrderByIdAsc();

        List<CampaignDto.FloorStatus> floorStatuses = new ArrayList<>();
        for (Floor floor : floors) {
            boolean cleared = progress.isFloorCleared(floor.getId());
            boolean locked = floor.getId() > 1 && !progress.isFloorCleared(floor.getId() - 1);

            String bossName = null;
            if (floor.getBossId() != null) {
                bossName = bossRepository.findById(floor.getBossId())
                        .map(boss -> boss.getName(language))
                        .orElse(null);
            }

            floorStatuses.add(CampaignDto.FloorStatus.builder()
                    .floor(floor.getId())
                    .floorType(floor.getFloorType().name())
                    .battleCount(floor.getBattleCount())
                    .cleared(cleared)
                    .locked(locked)
                    .description(floor.getDescription(language))
                    .aiLevel(floor.getAiLevel())
                    .bossId(floor.getBossId())
                    .bossName(bossName)
                    .skillRewardRarity(floor.getSkillRewardRarity() != null ?
                            floor.getSkillRewardRarity().name() : null)
                    .build());
        }

        Map<String, Boolean> bossesDefeated = new LinkedHashMap<>();
        bossesDefeated.put("mammon", Boolean.TRUE.equals(progress.getMammonDefeated()));
        bossesDefeated.put("eligor", Boolean.TRUE.equals(progress.getEligorDefeated()));
        bossesDefeated.put("lucifuge", Boolean.TRUE.equals(progress.getLucifugeDefeated()));

        return CampaignDto.CampaignProgressResponse.builder()
                .playerId(playerId)
                .currentRunFloor(progress.getCurrentRunFloor())
                .currentRunHp(progress.getCurrentRunHp())
                .floors(floorStatuses)
                .bossesDefeated(bossesDefeated)
                .totalRuns(progress.getTotalRuns())
                .totalVictories(progress.getTotalVictories())
                .totalDefeats(progress.getTotalDefeats())
                .build();
    }

    /**
     * Start a floor battle
     */
    @Transactional
    public CampaignDto.StartFloorResponse startFloor(
            Long playerId, int floorNum, List<Long> equippedSkillIds, String language) {

        CampaignProgress progress = getOrCreateProgress(playerId);

        // Validate floor access
        if (floorNum > 1 && !progress.isFloorCleared(floorNum - 1)) {
            throw new IllegalStateException("Floor " + floorNum + " is locked. Clear floor " + (floorNum - 1) + " first.");
        }

        Floor floor = floorRepository.findById(floorNum)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found: " + floorNum));

        // Determine battle index (how many battles already completed on this floor in current run)
        int completedBattles = countCompletedBattlesOnFloor(playerId, floorNum);
        int battleIndex = completedBattles + 1;

        if (battleIndex > floor.getBattleCount()) {
            throw new IllegalStateException("All battles on floor " + floorNum + " already completed.");
        }

        // Build enemy skills based on AI level
        List<Long> enemySkillIds = assignEnemySkills(floor);

        // Create battle request
        BattleDto.StartRequest battleRequest = BattleDto.StartRequest.builder()
                .playerId(playerId)
                .battleType("PVE")
                .floor(floorNum)
                .equippedSkills(equippedSkillIds)
                .bossId(floor.getBossId())
                .bossPhase(floor.getFloorType() == Floor.FloorType.BOSS ? 1 : null)
                .enemySkills(enemySkillIds)
                .build();

        BattleDto.StartResponse battleResponse = battleService.startBattle(battleRequest);

        // Update progress
        progress.setCurrentRunFloor(floorNum);
        progress.setTotalRuns(progress.getTotalRuns() + (battleIndex == 1 ? 1 : 0));
        campaignProgressRepository.save(progress);

        // Build response
        String bossName = null;
        Integer bossTotalPhases = null;
        if (floor.getBossId() != null) {
            Boss boss = bossRepository.findById(floor.getBossId()).orElse(null);
            if (boss != null) {
                bossName = boss.getName(language);
                bossTotalPhases = boss.getTotalPhases();
            }
        }

        return CampaignDto.StartFloorResponse.builder()
                .battleId(battleResponse.getBattleId())
                .floor(floorNum)
                .floorType(floor.getFloorType().name())
                .battleIndex(battleIndex)
                .totalBattles(floor.getBattleCount())
                .aiLevel(floor.getAiLevel())
                .enemySkillIds(enemySkillIds)
                .bossId(floor.getBossId())
                .bossName(bossName)
                .bossPhase(floor.getFloorType() == Floor.FloorType.BOSS ? 1 : null)
                .bossTotalPhases(bossTotalPhases)
                .build();
    }

    /**
     * Complete a battle on a floor (called after VICTORY)
     */
    @Transactional
    public CampaignDto.FloorCompleteResponse completeFloorBattle(
            Long playerId, Long battleId, String language) {

        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found: " + battleId));

        if (battle.getStatus() != Battle.Status.VICTORY) {
            throw new IllegalStateException("Battle is not victorious");
        }

        int floorNum = battle.getFloor();
        Floor floor = floorRepository.findById(floorNum)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found: " + floorNum));

        CampaignProgress progress = getOrCreateProgress(playerId);

        // Count completed battles on this floor
        int completedBattles = countCompletedBattlesOnFloor(playerId, floorNum);

        boolean floorCleared = completedBattles >= floor.getBattleCount();
        boolean skillRewardAvailable = false;
        List<CampaignDto.SkillRewardOption> offeredSkills = null;

        if (floorCleared) {
            // Mark floor as cleared
            progress.markFloorCleared(floorNum);
            progress.setTotalVictories(progress.getTotalVictories() + 1);

            // Mark boss defeated
            if (floor.getFloorType() == Floor.FloorType.BOSS && floor.getBossId() != null) {
                markBossDefeated(progress, floor.getBossId());
            }

            // Skill reward for boss floors
            if (floor.getSkillRewardRarity() != null) {
                skillRewardAvailable = true;
                offeredSkills = getSkillRewardOptionsInternal(playerId, floor, language);
            }

            campaignProgressRepository.save(progress);
        }

        Integer nextFloor = floorCleared && floorNum < 15 ? floorNum + 1 : null;

        return CampaignDto.FloorCompleteResponse.builder()
                .floor(floorNum)
                .cleared(floorCleared)
                .nextFloor(nextFloor)
                .skillRewardAvailable(skillRewardAvailable)
                .offeredSkills(offeredSkills)
                .build();
    }

    /**
     * Get skill reward options for a boss floor
     */
    @Transactional(readOnly = true)
    public List<CampaignDto.SkillRewardOption> getSkillRewardOptions(
            Long playerId, int floorNum, String language) {

        Floor floor = floorRepository.findById(floorNum)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found: " + floorNum));

        if (floor.getSkillRewardRarity() == null) {
            throw new IllegalStateException("Floor " + floorNum + " does not offer skill rewards");
        }

        return getSkillRewardOptionsInternal(playerId, floor, language);
    }

    /**
     * Select a skill reward
     */
    @Transactional
    public void selectSkillReward(Long playerId, int floorNum, Long selectedSkillId) {
        Floor floor = floorRepository.findById(floorNum)
                .orElseThrow(() -> new IllegalArgumentException("Floor not found: " + floorNum));

        if (floor.getSkillRewardRarity() == null) {
            throw new IllegalStateException("Floor " + floorNum + " does not offer skill rewards");
        }

        // Get the offered skills (same rarity, unowned)
        Skill.Rarity rarity = Skill.Rarity.valueOf(floor.getSkillRewardRarity().name());
        List<Skill> unownedSkills = skillRepository.findUnownedByRarity(rarity, playerId);

        // Unlock ALL offered skills (per CLAUDE.md: all 3 are permanently unlocked)
        // We unlock up to 3 skills of this rarity
        int count = 0;
        for (Skill skill : unownedSkills) {
            if (count >= 3) break;
            if (!playerSkillRepository.existsByPlayerIdAndSkillId(playerId, skill.getId())) {
                PlayerSkill ps = PlayerSkill.builder()
                        .playerId(playerId)
                        .skillId(skill.getId())
                        .build();
                playerSkillRepository.save(ps);
                count++;
            }
        }

        log.info("Player {} selected skill {} on floor {}. Unlocked {} skills total.",
                playerId, selectedSkillId, floorNum, count);
    }

    /**
     * Record a defeat on a floor
     */
    @Transactional
    public void recordDefeat(Long playerId) {
        CampaignProgress progress = getOrCreateProgress(playerId);
        progress.setTotalDefeats(progress.getTotalDefeats() + 1);
        campaignProgressRepository.save(progress);
    }

    // --- Internal helpers ---

    private CampaignProgress getOrCreateProgress(Long playerId) {
        return campaignProgressRepository.findByPlayerId(playerId)
                .orElseGet(() -> {
                    CampaignProgress newProgress = CampaignProgress.builder()
                            .playerId(playerId)
                            .build();
                    return campaignProgressRepository.save(newProgress);
                });
    }

    private int countCompletedBattlesOnFloor(Long playerId, int floor) {
        return battleRepository.countByPlayerIdAndFloorAndStatus(playerId, floor, Battle.Status.VICTORY);
    }

    private List<Long> assignEnemySkills(Floor floor) {
        int skillCount = floor.getEnemySkillCount();
        if (skillCount <= 0) return Collections.emptyList();

        // For boss floors, skills come from boss phase config
        if (floor.getFloorType() == Floor.FloorType.BOSS && floor.getBossId() != null) {
            return getBossPhaseSkills(floor.getBossId(), 1);
        }

        // For normal/elite floors, pick random skills based on AI level
        Skill.Rarity maxRarity = switch (floor.getAiLevel()) {
            case 0 -> Skill.Rarity.Common;
            case 1 -> Skill.Rarity.Rare;
            case 2 -> Skill.Rarity.Epic;
            default -> Skill.Rarity.Legendary;
        };

        List<Skill> candidates = skillRepository.findByRarity(maxRarity);
        if (candidates.isEmpty()) {
            candidates = skillRepository.findByRarity(Skill.Rarity.Common);
        }

        Collections.shuffle(candidates);
        return candidates.stream()
                .limit(skillCount)
                .map(Skill::getId)
                .collect(Collectors.toList());
    }

    /**
     * Get boss skills for a specific phase from phase_config JSON
     */
    public List<Long> getBossPhaseSkills(String bossId, int phase) {
        Boss boss = bossRepository.findById(bossId).orElse(null);
        if (boss == null) return Collections.emptyList();

        try {
            JsonNode config = objectMapper.readTree(boss.getPhaseConfig());
            JsonNode phaseNode = config.get("phase" + phase);
            if (phaseNode == null || !phaseNode.has("skills")) return Collections.emptyList();

            List<String> skillCodes = new ArrayList<>();
            phaseNode.get("skills").forEach(node -> skillCodes.add(node.asText()));

            // Convert skill codes to IDs
            return skillCodes.stream()
                    .map(code -> skillRepository.findBySkillCode(code).map(Skill::getId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse boss phase config for {} phase {}", bossId, phase, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get boss quote for a specific phase
     */
    public String getBossQuote(String bossId, int phase, String language) {
        Boss boss = bossRepository.findById(bossId).orElse(null);
        if (boss == null || boss.getQuotes() == null) return null;

        try {
            JsonNode quotes = objectMapper.readTree(boss.getQuotes());
            String key = "phase" + phase + "_" + language;
            if (quotes.has(key)) {
                return quotes.get(key).asText();
            }
            // Fallback to English
            String fallbackKey = "phase" + phase + "_en";
            if (quotes.has(fallbackKey)) {
                return quotes.get(fallbackKey).asText();
            }
            // Try intro
            String introKey = "intro_" + language;
            if (phase == 1 && quotes.has(introKey)) {
                return quotes.get(introKey).asText();
            }
        } catch (Exception e) {
            log.error("Failed to parse boss quotes for {}", bossId, e);
        }
        return null;
    }

    private void markBossDefeated(CampaignProgress progress, String bossId) {
        switch (bossId) {
            case "mammon" -> progress.setMammonDefeated(true);
            case "eligor" -> progress.setEligorDefeated(true);
            case "lucifuge" -> progress.setLucifugeDefeated(true);
        }
    }

    private List<CampaignDto.SkillRewardOption> getSkillRewardOptionsInternal(
            Long playerId, Floor floor, String language) {

        Skill.Rarity rarity = Skill.Rarity.valueOf(floor.getSkillRewardRarity().name());
        List<Skill> unownedSkills = skillRepository.findUnownedByRarity(rarity, playerId);

        Collections.shuffle(unownedSkills);
        return unownedSkills.stream()
                .limit(3)
                .map(skill -> {
                    String name = switch (language) {
                        case "ko" -> skill.getNameKo();
                        case "ja" -> skill.getNameJa();
                        case "zh" -> skill.getNameZh();
                        default -> skill.getNameEn();
                    };
                    String desc = switch (language) {
                        case "ko" -> skill.getDescriptionKo();
                        case "ja" -> skill.getDescriptionJa();
                        case "zh" -> skill.getDescriptionZh();
                        default -> skill.getDescriptionEn();
                    };
                    return CampaignDto.SkillRewardOption.builder()
                            .skillId(skill.getId())
                            .skillCode(skill.getSkillCode())
                            .name(name)
                            .description(desc)
                            .rarity(skill.getRarity().name())
                            .triggerType(skill.getTriggerType().name())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
