package com.hotelsortis.api.websocket;

import com.hotelsortis.api.dto.DraftDto;
import com.hotelsortis.api.service.DraftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for PvP Draft Mode
 *
 * Handles real-time draft picks and broadcasts state updates
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DraftWebSocketController {

    private final DraftService draftService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle skill pick from client
     *
     * Client sends to: /app/draft/{battleId}/pick
     * Server broadcasts to: /user/queue/draft/state
     */
    @MessageMapping("/draft/{battleId}/pick")
    public void handlePick(
            @DestinationVariable Long battleId,
            @Payload DraftDto.PickRequest request,
            @Header(value = "Accept-Language", defaultValue = "ko") String lang
    ) {
        log.info("Draft pick received: battleId={}, playerId={}, skillId={}",
                battleId, request.getPlayerId(), request.getSkillId());

        DraftDto.PickResponse response = draftService.pickSkill(
                battleId,
                request.getPlayerId(),
                request.getSkillId(),
                lang
        );

        if (!response.getSuccess()) {
            // Send error only to the player who made the invalid pick
            messagingTemplate.convertAndSendToUser(
                    request.getPlayerId().toString(),
                    "/queue/draft/error",
                    response
            );
            return;
        }

        // Get updated state and broadcast to both players
        DraftDto.DraftState state = draftService.getDraftState(battleId, lang);
        if (state != null) {
            // Send to player 1
            messagingTemplate.convertAndSendToUser(
                    state.getPlayer1Id().toString(),
                    "/queue/draft/state",
                    state
            );
            // Send to player 2
            messagingTemplate.convertAndSendToUser(
                    state.getPlayer2Id().toString(),
                    "/queue/draft/state",
                    state
            );

            // If all picks complete, notify both players
            if ("PICKS_COMPLETE".equals(state.getStatus())) {
                broadcastPicksComplete(state);
            }
        }

        // Also send the pick response for immediate feedback
        messagingTemplate.convertAndSendToUser(
                request.getPlayerId().toString(),
                "/queue/draft/pick",
                response
        );
    }

    /**
     * Handle player ready signal after draft picks complete
     *
     * Client sends to: /app/draft/{battleId}/ready
     */
    @MessageMapping("/draft/{battleId}/ready")
    public void handleReady(
            @DestinationVariable Long battleId,
            @Payload DraftDto.ReadyRequest request,
            @Header(value = "Accept-Language", defaultValue = "ko") String lang
    ) {
        log.info("Player ready: battleId={}, playerId={}", battleId, request.getPlayerId());

        boolean success = draftService.setPlayerReady(battleId, request.getPlayerId());
        if (!success) {
            log.warn("Failed to set player ready: battleId={}, playerId={}", battleId, request.getPlayerId());
            return;
        }

        // Check if both players are ready
        if (draftService.isDraftComplete(battleId)) {
            // Finalize draft and update battle
            DraftDto.DraftCompleteMessage completeMessage = draftService.finalizeDraft(battleId);

            // Get state for player IDs
            DraftDto.DraftState state = draftService.getDraftState(battleId, lang);
            if (state == null) {
                // Draft was cleaned up, get IDs from complete message
                // Broadcast to both players using battle ID pattern
                messagingTemplate.convertAndSend(
                        "/topic/draft/" + battleId + "/complete",
                        completeMessage
                );
            } else {
                // Send to both players
                messagingTemplate.convertAndSendToUser(
                        state.getPlayer1Id().toString(),
                        "/queue/draft/complete",
                        completeMessage
                );
                messagingTemplate.convertAndSendToUser(
                        state.getPlayer2Id().toString(),
                        "/queue/draft/complete",
                        completeMessage
                );
            }

            log.info("Draft complete: battleId={}", battleId);
        } else {
            // Notify both players of ready status update
            DraftDto.DraftState state = draftService.getDraftState(battleId, lang);
            if (state != null) {
                messagingTemplate.convertAndSendToUser(
                        state.getPlayer1Id().toString(),
                        "/queue/draft/state",
                        state
                );
                messagingTemplate.convertAndSendToUser(
                        state.getPlayer2Id().toString(),
                        "/queue/draft/state",
                        state
                );
            }
        }
    }

    /**
     * Broadcast that all picks are complete, waiting for ready
     */
    private void broadcastPicksComplete(DraftDto.DraftState state) {
        log.info("All picks complete for battle {}, waiting for ready", state.getBattleId());

        // Send notification to both players
        messagingTemplate.convertAndSendToUser(
                state.getPlayer1Id().toString(),
                "/queue/draft/picks-complete",
                state
        );
        messagingTemplate.convertAndSendToUser(
                state.getPlayer2Id().toString(),
                "/queue/draft/picks-complete",
                state
        );
    }

    /**
     * Send timer update to both players
     * Called by a scheduled task or timer service
     */
    public void broadcastTimerUpdate(Long battleId, DraftDto.TimerUpdate timerUpdate, Long player1Id, Long player2Id) {
        messagingTemplate.convertAndSendToUser(
                player1Id.toString(),
                "/queue/draft/timer",
                timerUpdate
        );
        messagingTemplate.convertAndSendToUser(
                player2Id.toString(),
                "/queue/draft/timer",
                timerUpdate
        );
    }
}
