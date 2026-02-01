package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.CampaignDto;
import com.hotelsortis.api.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CampaignController {

    private final CampaignService campaignService;

    private String resolveLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) return "en";
        String lang = Locale.forLanguageTag(acceptLanguage.split(",")[0].trim()).getLanguage();
        return List.of("ko", "en", "ja", "zh").contains(lang) ? lang : "en";
    }

    /**
     * Get campaign progress
     * GET /api/v1/campaigns/{playerId}
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<EntityModel<CampaignDto.CampaignProgressResponse>> getCampaignProgress(
            @PathVariable Long playerId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        String lang = resolveLanguage(acceptLanguage);
        log.info("Getting campaign progress for player: {} (lang={})", playerId, lang);

        CampaignDto.CampaignProgressResponse response = campaignService.getCampaignProgress(playerId, lang);
        EntityModel<CampaignDto.CampaignProgressResponse> model = EntityModel.of(response);

        return ResponseEntity.ok(model);
    }

    /**
     * Start a floor battle
     * POST /api/v1/campaigns/{playerId}/floors/{floor}/start
     */
    @PostMapping("/{playerId}/floors/{floor}/start")
    public ResponseEntity<EntityModel<CampaignDto.StartFloorResponse>> startFloor(
            @PathVariable Long playerId,
            @PathVariable Integer floor,
            @RequestBody CampaignDto.StartFloorRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        String lang = resolveLanguage(acceptLanguage);
        log.info("Starting floor {} for player: {} (lang={})", floor, playerId, lang);

        CampaignDto.StartFloorResponse response = campaignService.startFloor(
                playerId, floor, request.getEquippedSkillIds(), lang);

        EntityModel<CampaignDto.StartFloorResponse> model = EntityModel.of(response);
        return ResponseEntity.ok(model);
    }

    /**
     * Complete a floor battle (after VICTORY)
     * POST /api/v1/campaigns/battles/{battleId}/complete
     */
    @PostMapping("/battles/{battleId}/complete")
    public ResponseEntity<EntityModel<CampaignDto.FloorCompleteResponse>> completeFloorBattle(
            @PathVariable Long battleId,
            @RequestParam Long playerId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        String lang = resolveLanguage(acceptLanguage);
        log.info("Completing battle {} for player: {}", battleId, playerId);

        CampaignDto.FloorCompleteResponse response = campaignService.completeFloorBattle(
                playerId, battleId, lang);

        EntityModel<CampaignDto.FloorCompleteResponse> model = EntityModel.of(response);
        return ResponseEntity.ok(model);
    }

    /**
     * Get skill reward options for a boss floor
     * GET /api/v1/campaigns/{playerId}/floors/{floor}/rewards
     */
    @GetMapping("/{playerId}/floors/{floor}/rewards")
    public ResponseEntity<List<CampaignDto.SkillRewardOption>> getSkillRewards(
            @PathVariable Long playerId,
            @PathVariable Integer floor,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        String lang = resolveLanguage(acceptLanguage);
        log.info("Getting skill rewards for player: {} on floor: {}", playerId, floor);

        List<CampaignDto.SkillRewardOption> rewards = campaignService.getSkillRewardOptions(
                playerId, floor, lang);

        return ResponseEntity.ok(rewards);
    }

    /**
     * Select a skill reward
     * POST /api/v1/campaigns/{playerId}/floors/{floor}/rewards
     */
    @PostMapping("/{playerId}/floors/{floor}/rewards")
    public ResponseEntity<Void> selectSkillReward(
            @PathVariable Long playerId,
            @PathVariable Integer floor,
            @RequestBody CampaignDto.SelectSkillRewardRequest request
    ) {
        log.info("Player {} selecting skill {} on floor {}", playerId, request.getSelectedSkillId(), floor);

        campaignService.selectSkillReward(playerId, floor, request.getSelectedSkillId());

        return ResponseEntity.ok().build();
    }
}
