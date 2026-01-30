package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.BattleDto;
import com.hotelsortis.api.service.BattleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/battles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BattleController {

    private final BattleService battleService;

    /**
     * Start a new battle
     * POST /api/v1/battles
     */
    @PostMapping
    public ResponseEntity<EntityModel<BattleDto.StartResponse>> startBattle(
            @RequestBody BattleDto.StartRequest request
    ) {
        log.info("Starting battle for player: {}", request.getPlayerId());

        BattleDto.StartResponse response = battleService.startBattle(request);

        EntityModel<BattleDto.StartResponse> model = EntityModel.of(response);

        // HATEOAS links
        model.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(BattleController.class)
                        .getBattleStatus(response.getBattleId()))
                .withSelfRel());

        model.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(BattleController.class)
                        .rollDice(response.getBattleId(), null))
                .withRel("roll-dice"));

        return ResponseEntity.ok(model);
    }

    /**
     * Roll dice in current battle
     * POST /api/v1/battles/{battleId}/roll
     *
     * IMPORTANT: Dice are generated SERVER-SIDE only!
     */
    @PostMapping("/{battleId}/roll")
    public ResponseEntity<EntityModel<BattleDto.RollResponse>> rollDice(
            @PathVariable Long battleId,
            @RequestBody BattleDto.RollRequest request
    ) {
        log.info("Rolling dice for battle: {}, player: {}", battleId, request.getPlayerId());

        BattleDto.RollResponse response = battleService.rollDice(battleId, request);

        EntityModel<BattleDto.RollResponse> model = EntityModel.of(response);

        // HATEOAS links
        model.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(BattleController.class)
                        .getBattleStatus(battleId))
                .withSelfRel());

        // Add next action link based on battle status
        if ("ONGOING".equals(response.getStatus())) {
            model.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(BattleController.class)
                            .rollDice(battleId, null))
                    .withRel("roll-dice"));
        }

        return ResponseEntity.ok(model);
    }

    /**
     * Get current battle status
     * GET /api/v1/battles/{battleId}
     */
    @GetMapping("/{battleId}")
    public ResponseEntity<EntityModel<BattleDto.BattleStatus>> getBattleStatus(
            @PathVariable Long battleId
    ) {
        BattleDto.BattleStatus status = battleService.getBattleStatus(battleId);

        EntityModel<BattleDto.BattleStatus> model = EntityModel.of(status);

        model.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(BattleController.class)
                        .getBattleStatus(battleId))
                .withSelfRel());

        if ("ONGOING".equals(status.getStatus())) {
            model.add(WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(BattleController.class)
                            .rollDice(battleId, null))
                    .withRel("roll-dice"));
        }

        return ResponseEntity.ok(model);
    }
}
