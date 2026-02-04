package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.BattleDto;
import com.hotelsortis.api.dto.DraftDto;
import com.hotelsortis.api.dto.PvPDto;
import com.hotelsortis.api.entity.Battle;
import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.repository.BattleRepository;
import com.hotelsortis.api.repository.PlayerRepository;
import com.hotelsortis.api.service.BattleService;
import com.hotelsortis.api.service.DraftService;
import com.hotelsortis.api.service.MatchmakingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * PvP 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pvp")
@RequiredArgsConstructor
public class PvPController {

    private final MatchmakingService matchmakingService;
    private final BattleService battleService;
    private final DraftService draftService;
    private final PlayerRepository playerRepository;
    private final BattleRepository battleRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 매칭 대기열 참가 (재접속 처리 포함)
     */
    @PostMapping("/matchmaking/join")
    public ResponseEntity<EntityModel<PvPDto.JoinQueueResponse>> joinQueue(
        @RequestBody PvPDto.JoinQueueRequest request
    ) {
        Long playerId = request.getPlayerId();

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // 1. 진행 중인 PvP 전투가 있는지 확인 (재접속 처리)
        Optional<Battle> ongoingBattle = battleRepository.findOngoingPvPBattle(
            playerId,
            Battle.BattleType.PVP,
            Battle.Status.ONGOING
        );

        if (ongoingBattle.isPresent()) {
            Battle battle = ongoingBattle.get();
            log.info("Player {} has ongoing PvP battle {}, resuming instead of joining queue",
                playerId, battle.getId());

            PvPDto.JoinQueueResponse response = PvPDto.JoinQueueResponse.builder()
                .playerId(playerId)
                .elo(player.getElo())
                .queueSize(0L)
                .status("ALREADY_IN_BATTLE")
                .battleId(battle.getId())
                .build();

            EntityModel<PvPDto.JoinQueueResponse> resource = EntityModel.of(response);
            resource.add(linkTo(methodOn(BattleController.class).getBattleStatus(battle.getId()))
                .withRel("battle"));
            resource.add(Link.of("/ws", "websocket"));

            return ResponseEntity.ok(resource);
        }

        // 2. 이미 대기 중인지 확인
        if (matchmakingService.isInQueue(playerId)) {
            log.warn("Player {} already in matchmaking queue", playerId);
            return ResponseEntity.badRequest().build();
        }

        // 3. 대기열 참가
        matchmakingService.joinQueue(playerId);

        PvPDto.JoinQueueResponse response = PvPDto.JoinQueueResponse.builder()
            .playerId(playerId)
            .elo(player.getElo())
            .queueSize(matchmakingService.getQueueSize())
            .status("WAITING")
            .battleId(null)
            .build();

        EntityModel<PvPDto.JoinQueueResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(PvPController.class).leaveQueue(new PvPDto.LeaveQueueRequest(playerId)))
            .withRel("leave-queue"));
        resource.add(linkTo(methodOn(PvPController.class).findMatch(playerId))
            .withRel("find-match"));

        log.info("Player {} joined matchmaking queue (ELO: {})", playerId, player.getElo());

        return ResponseEntity.ok(resource);
    }

    /**
     * 상대 찾기 (폴링)
     */
    @GetMapping("/matchmaking/find/{playerId}")
    public ResponseEntity<EntityModel<PvPDto.MatchFoundResponse>> findMatch(
        @PathVariable Long playerId
    ) {
        // 상대 찾기 (원자적 연산: 찾기 + 큐에서 제거)
        // findOpponent()가 Lua Script로 매칭 확정까지 자동 처리
        Long opponentId = matchmakingService.findOpponent(playerId);

        if (opponentId == null) {
            // 아직 상대 없음
            return ResponseEntity.noContent().build();
        }

        // PvP 전투 생성
        Player player1 = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        Player player2 = playerRepository.findById(opponentId)
            .orElseThrow(() -> new IllegalArgumentException("Opponent not found"));

        // Create battle with draft mode (skills will be selected during draft)
        Battle battle = battleService.createPvPBattleWithDraft(player1, player2);

        // Initialize draft session
        draftService.initializeDraft(battle.getId(), playerId, opponentId, "ko");

        PvPDto.MatchFoundResponse response = PvPDto.MatchFoundResponse.builder()
            .battleId(battle.getId())
            .player1Id(playerId)
            .player2Id(opponentId)
            .player1Elo(player1.getElo())
            .player2Elo(player2.getElo())
            .status("MATCH_FOUND")
            .hasDraft(true) // Draft phase required before battle
            .build();

        EntityModel<PvPDto.MatchFoundResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(BattleController.class).getBattleStatus(battle.getId()))
            .withRel("battle"));
        resource.add(Link.of("/ws", "websocket"));
        resource.add(Link.of("/api/v1/pvp/draft/" + battle.getId() + "/pool", "draft-pool"));

        // WebSocket으로 양측에 매치 알림
        messagingTemplate.convertAndSendToUser(
            playerId.toString(),
            "/queue/match-found",
            response
        );
        messagingTemplate.convertAndSendToUser(
            opponentId.toString(),
            "/queue/match-found",
            response
        );

        log.info("Match created: Battle {} - Player {} vs Player {}", battle.getId(), playerId, opponentId);

        return ResponseEntity.ok(resource);
    }

    /**
     * 매칭 대기 취소
     */
    @PostMapping("/matchmaking/leave")
    public ResponseEntity<Void> leaveQueue(
        @RequestBody PvPDto.LeaveQueueRequest request
    ) {
        Long playerId = request.getPlayerId();

        matchmakingService.leaveQueue(playerId);

        log.info("Player {} left matchmaking queue", playerId);

        return ResponseEntity.ok().build();
    }

    /**
     * Get draft state for a battle
     */
    @GetMapping("/draft/{battleId}")
    public ResponseEntity<DraftDto.DraftState> getDraftState(
        @PathVariable Long battleId,
        @RequestHeader(value = "Accept-Language", defaultValue = "ko") String lang
    ) {
        DraftDto.DraftState state = draftService.getDraftState(battleId, lang);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(state);
    }

    /**
     * 랭크 정보 조회
     */
    @GetMapping("/rank/{playerId}")
    public ResponseEntity<EntityModel<PvPDto.RankInfoResponse>> getRankInfo(
        @PathVariable Long playerId
    ) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // 티어 계산
        String tier = calculateTier(player.getElo());

        // 전적 계산 (임시 - 나중에 별도 테이블로 관리)
        // TODO: PvPStats 엔티티 추가 후 실제 전적 조회
        int wins = 0;
        int losses = 0;
        int draws = 0;
        double winRate = 0.0;

        PvPDto.RankInfoResponse response = PvPDto.RankInfoResponse.builder()
            .playerId(playerId)
            .elo(player.getElo())
            .tier(tier)
            .wins(wins)
            .losses(losses)
            .draws(draws)
            .winRate(winRate)
            .build();

        EntityModel<PvPDto.RankInfoResponse> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(PvPController.class).getRankInfo(playerId))
            .withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /**
     * ELO 기반 티어 계산
     */
    private String calculateTier(int elo) {
        if (elo >= 2200) return "MASTER";
        if (elo >= 1900) return "DIAMOND";
        if (elo >= 1600) return "PLATINUM";
        if (elo >= 1300) return "GOLD";
        if (elo >= 1000) return "SILVER";
        return "BRONZE";
    }
}
