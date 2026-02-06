package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.SocialDto;
import com.hotelsortis.api.service.ChatService;
import com.hotelsortis.api.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Social system REST API controller
 * Handles friends, blocks, and chat operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SocialController {

    private final FriendService friendService;
    private final ChatService chatService;

    // ==================== Friend Endpoints ====================

    /**
     * Send a friend request
     * POST /api/v1/social/friends/request?playerId=123
     */
    @PostMapping("/friends/request")
    public ResponseEntity<SocialDto.FriendResponse> sendFriendRequest(
            @RequestParam Long playerId,
            @RequestBody SocialDto.FriendRequest request
    ) {
        log.info("POST /api/v1/social/friends/request - playerId: {}, target: {}",
                playerId, request.getTargetPlayerId());

        SocialDto.FriendResponse response = friendService.sendFriendRequest(playerId, request.getTargetPlayerId());
        return ResponseEntity.ok(response);
    }

    /**
     * Accept a friend request
     * POST /api/v1/social/friends/accept/{requestId}?playerId=123
     */
    @PostMapping("/friends/accept/{requestId}")
    public ResponseEntity<SocialDto.FriendResponse> acceptFriendRequest(
            @PathVariable Long requestId,
            @RequestParam Long playerId
    ) {
        log.info("POST /api/v1/social/friends/accept/{} - playerId: {}", requestId, playerId);

        SocialDto.FriendResponse response = friendService.acceptFriendRequest(playerId, requestId);
        return ResponseEntity.ok(response);
    }

    /**
     * Decline a friend request
     * DELETE /api/v1/social/friends/request/{requestId}?playerId=123
     */
    @DeleteMapping("/friends/request/{requestId}")
    public ResponseEntity<SocialDto.SuccessResponse> declineFriendRequest(
            @PathVariable Long requestId,
            @RequestParam Long playerId
    ) {
        log.info("DELETE /api/v1/social/friends/request/{} - playerId: {}", requestId, playerId);

        friendService.declineFriendRequest(playerId, requestId);
        return ResponseEntity.ok(SocialDto.SuccessResponse.builder()
                .success(true)
                .message("Friend request declined")
                .build());
    }

    /**
     * Remove a friend
     * DELETE /api/v1/social/friends/{friendId}?playerId=123
     */
    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<SocialDto.SuccessResponse> removeFriend(
            @PathVariable Long friendId,
            @RequestParam Long playerId
    ) {
        log.info("DELETE /api/v1/social/friends/{} - playerId: {}", friendId, playerId);

        friendService.removeFriend(playerId, friendId);
        return ResponseEntity.ok(SocialDto.SuccessResponse.builder()
                .success(true)
                .message("Friend removed")
                .build());
    }

    /**
     * Get friend list
     * GET /api/v1/social/friends?playerId=123
     */
    @GetMapping("/friends")
    public ResponseEntity<SocialDto.FriendListResponse> getFriends(
            @RequestParam Long playerId
    ) {
        log.info("GET /api/v1/social/friends - playerId: {}", playerId);

        SocialDto.FriendListResponse response = friendService.getFriends(playerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending friend requests
     * GET /api/v1/social/friends/pending?playerId=123
     */
    @GetMapping("/friends/pending")
    public ResponseEntity<SocialDto.PendingRequestsResponse> getPendingRequests(
            @RequestParam Long playerId
    ) {
        log.info("GET /api/v1/social/friends/pending - playerId: {}", playerId);

        SocialDto.PendingRequestsResponse response = friendService.getPendingRequests(playerId);
        return ResponseEntity.ok(response);
    }

    // ==================== Block Endpoints ====================

    /**
     * Block a player
     * POST /api/v1/social/blocks?playerId=123
     */
    @PostMapping("/blocks")
    public ResponseEntity<SocialDto.BlockedPlayerResponse> blockPlayer(
            @RequestParam Long playerId,
            @RequestBody SocialDto.BlockRequest request
    ) {
        log.info("POST /api/v1/social/blocks - playerId: {}, target: {}",
                playerId, request.getTargetPlayerId());

        SocialDto.BlockedPlayerResponse response = friendService.blockPlayer(playerId, request.getTargetPlayerId());
        return ResponseEntity.ok(response);
    }

    /**
     * Unblock a player
     * DELETE /api/v1/social/blocks/{blockedId}?playerId=123
     */
    @DeleteMapping("/blocks/{blockedId}")
    public ResponseEntity<SocialDto.SuccessResponse> unblockPlayer(
            @PathVariable Long blockedId,
            @RequestParam Long playerId
    ) {
        log.info("DELETE /api/v1/social/blocks/{} - playerId: {}", blockedId, playerId);

        friendService.unblockPlayer(playerId, blockedId);
        return ResponseEntity.ok(SocialDto.SuccessResponse.builder()
                .success(true)
                .message("Player unblocked")
                .build());
    }

    /**
     * Get blocked players list
     * GET /api/v1/social/blocks?playerId=123
     */
    @GetMapping("/blocks")
    public ResponseEntity<SocialDto.BlockListResponse> getBlockedPlayers(
            @RequestParam Long playerId
    ) {
        log.info("GET /api/v1/social/blocks - playerId: {}", playerId);

        SocialDto.BlockListResponse response = friendService.getBlockedPlayers(playerId);
        return ResponseEntity.ok(response);
    }

    // ==================== Chat Endpoints ====================

    /**
     * Send a chat message (global or whisper)
     * POST /api/v1/social/chat?playerId=123
     */
    @PostMapping("/chat")
    public ResponseEntity<SocialDto.ChatMessageResponse> sendMessage(
            @RequestParam Long playerId,
            @RequestBody SocialDto.SendMessageRequest request
    ) {
        log.info("POST /api/v1/social/chat - playerId: {}, type: {}", playerId, request.getMessageType());

        SocialDto.ChatMessageResponse response;
        if ("GLOBAL".equalsIgnoreCase(request.getMessageType())) {
            response = chatService.sendGlobalMessage(playerId, request.getContent());
        } else if ("WHISPER".equalsIgnoreCase(request.getMessageType())) {
            if (request.getReceiverId() == null) {
                throw new IllegalArgumentException("Receiver ID is required for whisper messages");
            }
            response = chatService.sendWhisper(playerId, request.getReceiverId(), request.getContent());
        } else {
            throw new IllegalArgumentException("Invalid message type: " + request.getMessageType());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get global chat history
     * GET /api/v1/social/chat/global?page=0&size=50
     */
    @GetMapping("/chat/global")
    public ResponseEntity<SocialDto.ChatHistoryResponse> getGlobalMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        log.info("GET /api/v1/social/chat/global - page: {}, size: {}", page, size);

        SocialDto.ChatHistoryResponse response = chatService.getGlobalMessages(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get whisper conversation
     * GET /api/v1/social/chat/whisper/{partnerId}?playerId=123&page=0&size=50
     */
    @GetMapping("/chat/whisper/{partnerId}")
    public ResponseEntity<SocialDto.ChatHistoryResponse> getWhisperConversation(
            @PathVariable Long partnerId,
            @RequestParam Long playerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        log.info("GET /api/v1/social/chat/whisper/{} - playerId: {}", partnerId, playerId);

        SocialDto.ChatHistoryResponse response = chatService.getWhisperConversation(playerId, partnerId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark message as read
     * PATCH /api/v1/social/chat/{messageId}/read?playerId=123
     */
    @PatchMapping("/chat/{messageId}/read")
    public ResponseEntity<SocialDto.SuccessResponse> markMessageAsRead(
            @PathVariable Long messageId,
            @RequestParam Long playerId
    ) {
        log.info("PATCH /api/v1/social/chat/{}/read - playerId: {}", messageId, playerId);

        chatService.markMessageAsRead(messageId, playerId);
        return ResponseEntity.ok(SocialDto.SuccessResponse.builder()
                .success(true)
                .message("Message marked as read")
                .build());
    }

    /**
     * Mark conversation as read
     * PATCH /api/v1/social/chat/conversation/{senderId}/read?playerId=123
     */
    @PatchMapping("/chat/conversation/{senderId}/read")
    public ResponseEntity<SocialDto.SuccessResponse> markConversationAsRead(
            @PathVariable Long senderId,
            @RequestParam Long playerId
    ) {
        log.info("PATCH /api/v1/social/chat/conversation/{}/read - playerId: {}", senderId, playerId);

        int count = chatService.markConversationAsRead(playerId, senderId);
        return ResponseEntity.ok(SocialDto.SuccessResponse.builder()
                .success(true)
                .message("Marked " + count + " messages as read")
                .build());
    }

    /**
     * Get unread message count
     * GET /api/v1/social/chat/unread/count?playerId=123
     */
    @GetMapping("/chat/unread/count")
    public ResponseEntity<SocialDto.UnreadCountResponse> getUnreadCount(
            @RequestParam Long playerId
    ) {
        log.info("GET /api/v1/social/chat/unread/count - playerId: {}", playerId);

        int count = chatService.getUnreadCount(playerId);
        return ResponseEntity.ok(SocialDto.UnreadCountResponse.builder()
                .unreadCount(count)
                .build());
    }

    /**
     * Get unread whispers
     * GET /api/v1/social/chat/unread?playerId=123
     */
    @GetMapping("/chat/unread")
    public ResponseEntity<List<SocialDto.ChatMessageResponse>> getUnreadWhispers(
            @RequestParam Long playerId
    ) {
        log.info("GET /api/v1/social/chat/unread - playerId: {}", playerId);

        List<SocialDto.ChatMessageResponse> unread = chatService.getUnreadWhispers(playerId);
        return ResponseEntity.ok(unread);
    }
}
