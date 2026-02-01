package com.hotelsortis.api.websocket;

import com.hotelsortis.api.dto.BattleDto;
import com.hotelsortis.api.dto.PvPDto;
import com.hotelsortis.api.entity.Battle;
import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.repository.BattleRepository;
import com.hotelsortis.api.repository.PlayerRepository;
import com.hotelsortis.api.service.BattleService;
import com.hotelsortis.api.service.EloCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * PvP WebSocket (STOMP) 메시지 핸들러
 *
 * - 실시간 PvP 전투 메시지 처리
 * - 턴 동기화
 * - 주사위 결과 브로드캐스트
 * - 전투 종료 처리
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PvPWebSocketController {

    private final BattleService battleService;
    private final BattleRepository battleRepository;
    private final PlayerRepository playerRepository;
    private final EloCalculator eloCalculator;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * PvP 주사위 굴림
     *
     * @param battleId 전투 ID
     * @param request 주사위 굴림 요청
     */
    @MessageMapping("/pvp/battles/{battleId}/roll")
    public void rollDice(
        @DestinationVariable Long battleId,
        @Payload BattleDto.RollRequest request
    ) {
        try {
            // 전투 조회
            Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found: " + battleId));

            // PvP 전투인지 확인
            if (battle.getBattleType() != Battle.BattleType.PVP) {
                throw new IllegalArgumentException("Not a PvP battle: " + battleId);
            }

            // 턴 검증
            Long playerId = request.getPlayerId();
            boolean isPlayer1 = playerId.equals(battle.getPlayerId());
            boolean isPlayer2 = playerId.equals(battle.getEnemyId());

            if (!isPlayer1 && !isPlayer2) {
                throw new IllegalArgumentException("Player not in this battle: " + playerId);
            }

            boolean isPlayerTurn = (isPlayer1 && battle.getCurrentTurn() == Battle.TurnActor.PLAYER) ||
                                   (isPlayer2 && battle.getCurrentTurn() == Battle.TurnActor.ENEMY);

            if (!isPlayerTurn) {
                log.warn("Not player's turn: battleId={}, playerId={}", battleId, playerId);
                return;
            }

            // 주사위 굴림
            BattleDto.RollResponse rollResult = battleService.rollDice(battleId, request);

            // 상대방 ID 찾기
            Long opponentId = isPlayer1 ? battle.getEnemyId() : battle.getPlayerId();

            // 양쪽 플레이어에게 결과 전송
            PvPDto.DiceResultMessage message = PvPDto.DiceResultMessage.builder()
                .battleId(battleId)
                .playerId(playerId)
                .dice(rollResult.getDice())
                .handRank(rollResult.getHand().getRank())
                .handRankKR(rollResult.getHand().getRankKR())
                .handPower(rollResult.getHand().getPower())
                .damage(rollResult.getDamage())
                .opponentHp(isPlayer1 ? rollResult.getEnemyHp() : rollResult.getPlayerHp())
                .build();

            // 본인에게 전송
            messagingTemplate.convertAndSendToUser(
                playerId.toString(),
                "/queue/pvp/dice-result",
                message
            );

            // 상대에게 전송
            messagingTemplate.convertAndSendToUser(
                opponentId.toString(),
                "/queue/pvp/dice-result",
                message
            );

            // 전투 종료 확인
            Battle updatedBattle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle not found"));

            if (updatedBattle.getStatus() != Battle.Status.ONGOING) {
                // 전투 종료 처리
                handleBattleEnd(updatedBattle);
            } else {
                // 다음 턴 시작 알림
                sendTurnStartMessage(updatedBattle);
            }

        } catch (Exception e) {
            log.error("Error processing PvP dice roll", e);
            // 에러를 요청한 플레이어에게만 전송
            messagingTemplate.convertAndSendToUser(
                request.getPlayerId().toString(),
                "/queue/errors",
                "주사위 굴림 실패: " + e.getMessage()
            );
        }
    }

    /**
     * 전투 종료 처리
     */
    private void handleBattleEnd(Battle battle) {
        Long player1Id = battle.getPlayerId();
        Long player2Id = battle.getEnemyId();

        Player player1 = playerRepository.findById(player1Id)
            .orElseThrow(() -> new IllegalArgumentException("Player1 not found"));
        Player player2 = playerRepository.findById(player2Id)
            .orElseThrow(() -> new IllegalArgumentException("Player2 not found"));

        // 결과 판정
        String player1Result;
        String player2Result;
        double player1Score; // 1.0 = 승리, 0.5 = 무승부, 0.0 = 패배
        double player2Score;

        if (battle.getStatus() == Battle.Status.VICTORY) {
            player1Result = "VICTORY";
            player2Result = "DEFEAT";
            player1Score = 1.0;
            player2Score = 0.0;
        } else if (battle.getStatus() == Battle.Status.DEFEAT) {
            player1Result = "DEFEAT";
            player2Result = "VICTORY";
            player1Score = 0.0;
            player2Score = 1.0;
        } else {
            player1Result = "DRAW";
            player2Result = "DRAW";
            player1Score = 0.5;
            player2Score = 0.5;
        }

        // ELO 계산
        int player1EloChange = eloCalculator.calculateEloChange(player1.getElo(), player2.getElo(), player1Score);
        int player2EloChange = eloCalculator.calculateEloChange(player2.getElo(), player1.getElo(), player2Score);

        // ELO 업데이트
        player1.setElo(player1.getElo() + player1EloChange);
        player2.setElo(player2.getElo() + player2EloChange);
        playerRepository.save(player1);
        playerRepository.save(player2);

        // 영혼석 보상 (승리: 20, 패배: 5, 무승부: 10)
        int player1SoulStones = player1Score == 1.0 ? 20 : player1Score == 0.5 ? 10 : 5;
        int player2SoulStones = player2Score == 1.0 ? 20 : player2Score == 0.5 ? 10 : 5;

        player1.setSoulStones(player1.getSoulStones() + player1SoulStones);
        player2.setSoulStones(player2.getSoulStones() + player2SoulStones);
        playerRepository.save(player1);
        playerRepository.save(player2);

        // 보상 정보
        PvPDto.RewardInfo player1Reward = PvPDto.RewardInfo.builder()
            .eloChange(player1EloChange)
            .soulStones(player1SoulStones)
            .result(player1Result)
            .build();

        PvPDto.RewardInfo player2Reward = PvPDto.RewardInfo.builder()
            .eloChange(player2EloChange)
            .soulStones(player2SoulStones)
            .result(player2Result)
            .build();

        // 양쪽에 전투 종료 메시지 전송
        PvPDto.BattleEndMessage player1Message = PvPDto.BattleEndMessage.builder()
            .battleId(battle.getId())
            .result(player1Result)
            .reward(player1Reward)
            .build();

        PvPDto.BattleEndMessage player2Message = PvPDto.BattleEndMessage.builder()
            .battleId(battle.getId())
            .result(player2Result)
            .reward(player2Reward)
            .build();

        messagingTemplate.convertAndSendToUser(
            player1Id.toString(),
            "/queue/pvp/battle-end",
            player1Message
        );

        messagingTemplate.convertAndSendToUser(
            player2Id.toString(),
            "/queue/pvp/battle-end",
            player2Message
        );

        log.info("PvP battle {} ended: {} vs {} - ELO changes: {}/{}",
            battle.getId(), player1Id, player2Id, player1EloChange, player2EloChange);
    }

    /**
     * 다음 턴 시작 알림
     */
    private void sendTurnStartMessage(Battle battle) {
        Long player1Id = battle.getPlayerId();
        Long player2Id = battle.getEnemyId();

        String currentTurn = battle.getCurrentTurn() == Battle.TurnActor.PLAYER ? "PLAYER" : "ENEMY";

        PvPDto.TurnStartMessage message = PvPDto.TurnStartMessage.builder()
            .battleId(battle.getId())
            .currentTurn(currentTurn)
            .turnNumber(battle.getTurnCount())
            .timeRemaining(30000L) // 30초
            .build();

        // 양쪽 플레이어에게 전송
        messagingTemplate.convertAndSendToUser(
            player1Id.toString(),
            "/queue/pvp/turn-start",
            message
        );

        messagingTemplate.convertAndSendToUser(
            player2Id.toString(),
            "/queue/pvp/turn-start",
            message
        );
    }
}
