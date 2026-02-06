package com.hotelsortis.api.websocket;

import com.hotelsortis.api.dto.SocialDto;
import com.hotelsortis.api.service.ChatService;
import com.hotelsortis.api.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Chat WebSocket (STOMP) message handler
 *
 * - Real-time global chat broadcast
 * - Real-time whisper (DM) delivery
 * - Friend request notifications
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final FriendService friendService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send global chat message
     * Client sends to: /app/chat/global
     * Broadcasts to: /topic/chat/global
     */
    @MessageMapping("/chat/global")
    public void sendGlobalMessage(@Payload GlobalMessageRequest request) {
        try {
            log.info("WebSocket global message from player {}", request.getSenderId());

            // Save message and get response
            SocialDto.ChatMessageResponse response = chatService.sendGlobalMessage(
                    request.getSenderId(),
                    request.getContent()
            );

            // Broadcast to all subscribers
            messagingTemplate.convertAndSend("/topic/chat/global", response);

            log.info("Global message broadcast: {}", response.getId());
        } catch (Exception e) {
            log.error("Error sending global message", e);
            // Send error back to sender
            messagingTemplate.convertAndSendToUser(
                    request.getSenderId().toString(),
                    "/queue/errors",
                    new ErrorResponse("Failed to send message: " + e.getMessage())
            );
        }
    }

    /**
     * Send whisper (DM) to a specific player
     * Client sends to: /app/chat/whisper/{receiverId}
     * Delivers to: /user/{receiverId}/queue/whisper
     */
    @MessageMapping("/chat/whisper/{receiverId}")
    public void sendWhisper(
            @DestinationVariable Long receiverId,
            @Payload WhisperRequest request
    ) {
        try {
            log.info("WebSocket whisper from {} to {}", request.getSenderId(), receiverId);

            // Check if blocked
            if (friendService.isBlocked(request.getSenderId(), receiverId)) {
                throw new IllegalArgumentException("Cannot send message to blocked player");
            }

            // Save message and get response
            SocialDto.ChatMessageResponse response = chatService.sendWhisper(
                    request.getSenderId(),
                    receiverId,
                    request.getContent()
            );

            // Send to receiver
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/whisper",
                    response
            );

            // Send confirmation to sender
            messagingTemplate.convertAndSendToUser(
                    request.getSenderId().toString(),
                    "/queue/whisper",
                    response
            );

            log.info("Whisper delivered: {}", response.getId());
        } catch (Exception e) {
            log.error("Error sending whisper", e);
            messagingTemplate.convertAndSendToUser(
                    request.getSenderId().toString(),
                    "/queue/errors",
                    new ErrorResponse("Failed to send whisper: " + e.getMessage())
            );
        }
    }

    /**
     * Mark messages as read
     * Client sends to: /app/chat/read/{senderId}
     */
    @MessageMapping("/chat/read/{senderId}")
    public void markAsRead(
            @DestinationVariable Long senderId,
            @Payload ReadRequest request
    ) {
        try {
            log.info("Marking messages from {} as read by {}", senderId, request.getPlayerId());

            int count = chatService.markConversationAsRead(request.getPlayerId(), senderId);

            // Notify sender that messages were read
            messagingTemplate.convertAndSendToUser(
                    senderId.toString(),
                    "/queue/read-receipt",
                    new ReadReceiptResponse(request.getPlayerId(), count)
            );

            log.info("Marked {} messages as read", count);
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
        }
    }

    /**
     * Send friend request notification
     * Called by FriendService when a friend request is sent
     */
    public void notifyFriendRequest(Long targetPlayerId, SocialDto.FriendRequestResponse request) {
        log.info("Sending friend request notification to player {}", targetPlayerId);

        messagingTemplate.convertAndSendToUser(
                targetPlayerId.toString(),
                "/queue/friend-request",
                request
        );
    }

    /**
     * Send friend accepted notification
     * Called by FriendService when a friend request is accepted
     */
    public void notifyFriendAccepted(Long playerId, SocialDto.FriendResponse friend) {
        log.info("Sending friend accepted notification to player {}", playerId);

        messagingTemplate.convertAndSendToUser(
                playerId.toString(),
                "/queue/friend-accepted",
                friend
        );
    }

    // ==================== Request/Response DTOs ====================

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GlobalMessageRequest {
        private Long senderId;
        private String content;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WhisperRequest {
        private Long senderId;
        private String content;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReadRequest {
        private Long playerId;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReadReceiptResponse {
        private Long readerId;
        private int count;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }
}
